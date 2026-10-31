/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.nodejs.newProjectOrModule;

import consulo.application.progress.Task;
import consulo.content.bundle.Sdk;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.ConfigurationTypeUtil;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfiguration;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.nodejs.run.NpmScriptRunConfiguration;
import org.jspecify.annotations.Nullable;
import consulo.localize.LocalizeValue;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import consulo.project.Project;
import consulo.project.startup.StartupManager;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationService;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Generates the files of a new module with an npm package run by {@code npx} ({@code create-vue}, {@code nuxi init},
 * ...), like IntelliJ's {@code NpmPackageProjectGenerator}: when the project is opened, the package runs in a
 * temporary directory (generators refuse non-empty directories) and the generated files are moved into the module.
 *
 * @since 2026-10-31
 */
public final class NpxProjectGenerator {
    /**
     * Stands for the project name (the name of the module directory) in the arguments; without it the name is the
     * first argument.
     */
    public static final String PROJECT_NAME = "$PROJECT_NAME$";

    private static final int TIMEOUT_MILLIS = 10 * 60 * 1000;

    private NpxProjectGenerator() {
    }

    /**
     * @param npxPackage the package with its version ({@code create-vue@latest})
     * @param arguments  the arguments after the project name, or with {@link #PROJECT_NAME} in its place
     */
    public static void generateWhenOpened(Project project, Sdk sdk, Path moduleDirectory, String npxPackage, List<String> arguments) {
        generateWhenOpened(project, sdk, moduleDirectory, npxPackage, arguments, null);
    }

    /**
     * @param runScript the script of the generated {@code package.json} to run ({@code dev}, {@code start}): an npm
     *                  script run configuration is created and selected after the generation (JetBrains:
     *                  {@code CreateRunConfigurationUtil.npmConfiguration})
     */
    public static void generateWhenOpened(Project project, Sdk sdk, Path moduleDirectory, String npxPackage, List<String> arguments,
                                          @Nullable String runScript) {
        StartupManager.getInstance(project).runAfterOpened(() -> Task.Backgroundable.queue(project,
            LocalizeValue.localizeTODO("Generating project with " + npxPackage),
            indicator -> {
                if (generate(project, sdk, moduleDirectory, npxPackage, arguments) && runScript != null) {
                    project.getApplication().invokeLater(() -> createRunConfiguration(project, moduleDirectory, runScript));
                }
            }));
    }

    private static void createRunConfiguration(Project project, Path moduleDirectory, String script) {
        ConfigurationType type = ConfigurationTypeUtil.findConfigurationType(NpmScriptRunConfiguration.TYPE_ID);
        if (type == null || type.getConfigurationFactories().length == 0 || project.isDisposed()) {
            return;
        }
        RunManager runManager = RunManager.getInstance(project);
        RunnerAndConfigurationSettings settings = runManager.createRunConfiguration(script, type.getConfigurationFactories()[0]);
        RunConfiguration configuration = settings.getConfiguration();
        if (configuration instanceof NpmScriptRunConfiguration npmScript) {
            npmScript.setScript(script);
        }
        VirtualFile directory = LocalFileSystem.getInstance().findFileByNioFile(moduleDirectory);
        Module module = directory != null ? project.getApplication().runReadAction((java.util.function.Supplier<Module>) () -> ModuleUtilCore.findModuleForFile(directory, project)) : null;
        if (module != null && configuration instanceof ModuleBasedConfiguration<?> moduleBased) {
            moduleBased.setModule(module);
        }
        runManager.addConfiguration(settings);
        runManager.setSelectedConfiguration(settings);
    }

    /**
     * @return whether the files were generated
     */
    private static boolean generate(Project project, Sdk sdk, Path moduleDirectory, String npxPackage, List<String> arguments) {
        Path temp = null;
        try {
            temp = Files.createTempDirectory("consulo-npx");
            String projectName = moduleDirectory.getFileName().toString();

            Path home = sdk.getHomeNioPath();
            Path npx = NodeJSBundleType.getExePath(sdk.getPlatform(), home, "npx.cmd", "npx");
            List<String> parameters = new ArrayList<>();
            parameters.add("--yes");
            parameters.add(npxPackage);
            if (!arguments.contains(PROJECT_NAME)) {
                parameters.add(projectName);
            }
            for (String argument : arguments) {
                parameters.add(PROJECT_NAME.equals(argument) ? projectName : argument);
            }

            GeneralCommandLine commandLine = new GeneralCommandLine()
                .withExePath(npx.toString())
                .withParameters(parameters)
                .withWorkDirectory(temp.toString());
            // npx starts node through "#!/usr/bin/env node": the node of the bundle comes first
            Path nodeDirectory = NodeJSBundleType.getExePath(sdk).getParent();
            if (nodeDirectory != null) {
                String path = Platform.current().os().getEnvironmentVariable("PATH");
                commandLine.withEnvironment("PATH", nodeDirectory + File.pathSeparator + (path == null ? "" : path));
            }

            ProcessOutput output = CapturingProcessUtil.execAndGetOutput(commandLine, TIMEOUT_MILLIS);
            Path generated = temp.resolve(projectName);
            if (output.getExitCode() != 0 || !Files.isDirectory(generated)) {
                notifyError(project, npxPackage + " failed (exit code " + output.getExitCode() + "):\n" + tail(output.getStderr() + output.getStdout()));
                return false;
            }

            moveContent(generated, moduleDirectory);

            VirtualFile directory = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(moduleDirectory);
            if (directory != null) {
                directory.refresh(true, true);
            }
            NotificationService.getInstance()
                .newInfo(NotificationGroup.SYSTEM_MESSAGES)
                .title(LocalizeValue.localizeTODO("Project generated"))
                .content(LocalizeValue.localizeTODO("Generated by " + npxPackage + ". Run 'npm install' to install the dependencies."))
                .notify(project);
            return true;
        }
        catch (ExecutionException | IOException e) {
            notifyError(project, npxPackage + " failed: " + e.getMessage());
            return false;
        }
        finally {
            if (temp != null) {
                deleteRecursively(temp);
            }
        }
    }

    private static void moveContent(Path from, Path to) throws IOException {
        try (Stream<Path> children = Files.list(from)) {
            for (Path child : children.toList()) {
                Path target = to.resolve(child.getFileName().toString());
                if (Files.isDirectory(child) && Files.isDirectory(target)) {
                    moveContent(child, target);
                }
                else {
                    Files.move(child, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static void deleteRecursively(Path path) {
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                }
                catch (IOException ignored) {
                }
            });
        }
        catch (IOException ignored) {
        }
    }

    private static String tail(String text) {
        return text.length() > 2000 ? "..." + text.substring(text.length() - 2000) : text;
    }

    private static void notifyError(Project project, String message) {
        NotificationService.getInstance()
            .newError(NotificationGroup.SYSTEM_MESSAGES)
            .title(LocalizeValue.localizeTODO("Project generation failed"))
            .content(LocalizeValue.localizeTODO(message))
            .notify(project);
    }
}
