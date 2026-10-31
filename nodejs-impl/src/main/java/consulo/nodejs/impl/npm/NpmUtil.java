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
package consulo.nodejs.impl.npm;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.ReadAction;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.externalSystem.util.ExternalSystemConstants;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.nodejs.impl.npm.setting.NpmExecutionSettings;
import consulo.nodejs.impl.npm.setting.NpmProjectSettings;
import consulo.nodejs.impl.npm.setting.NpmSettings;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.nodejs.npm.NpmConstants;
import consulo.process.cmd.GeneralCommandLine;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 2026-10-31
 */
public final class NpmUtil {
    public static final String DEFAULT_EXECUTABLE = "npm";

    private NpmUtil() {
    }

    /**
     * A package may be linked by its directory or by its {@code package.json} - both name the same settings.
     */
    public static @Nullable NpmProjectSettings findProjectSettings(Project project, String packagePath) {
        Path packageDirectory = toPackageDirectory(Path.of(packagePath));
        for (NpmProjectSettings settings : NpmSettings.getInstance(project).getLinkedProjectsSettings()) {
            String linkedProjectPath = settings.getExternalProjectPath();
            if (linkedProjectPath != null && toPackageDirectory(Path.of(linkedProjectPath)).equals(packageDirectory)) {
                return settings;
            }
        }
        return null;
    }

    /**
     * @param fileOrDirectory {@code package.json} or the directory holding it
     */
    public static Path toPackageDirectory(Path fileOrDirectory) {
        if (Files.isDirectory(fileOrDirectory)) {
            return fileOrDirectory;
        }
        Path fileName = fileOrDirectory.getFileName();
        if (fileName != null && NpmConstants.PACKAGE_JSON.equals(fileName.toString())) {
            Path parent = fileOrDirectory.getParent();
            return parent == null ? fileOrDirectory : parent;
        }
        return fileOrDirectory;
    }

    /**
     * The module whose content root is the package directory
     */
    @RequiredReadAction
    public static @Nullable Module findModule(Project project, Path packageDirectory) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
                if (contentRoot.isInLocalFileSystem() && contentRoot.toNioPath().equals(packageDirectory)) {
                    return module;
                }
            }
        }
        return null;
    }

    /**
     * @return the id of the external system owning the module (Maven, ...) when it is not npm, {@code null} for a module
     * of npm or of no build system
     */
    public static @Nullable String getForeignOwner(Module module) {
        String owner = ExternalSystemApiUtil.getExtensionSystemOption(module, ExternalSystemConstants.EXTERNAL_SYSTEM_ID_KEY);
        return owner == null || owner.isEmpty() || NpmSystem.SYSTEM_ID.getId().equals(owner) ? null : owner;
    }

    /**
     * @return whether the module is imported from a linked npm package
     */
    public static boolean isLinked(Module module) {
        String owner = ExternalSystemApiUtil.getExtensionSystemOption(module, ExternalSystemConstants.EXTERNAL_SYSTEM_ID_KEY);
        return NpmSystem.SYSTEM_ID.getId().equals(owner);
    }

    public static NpmExecutionSettings createExecutionSettings(Project project, String linkedProjectPath) {
        NpmProjectSettings settings = findProjectSettings(project, linkedProjectPath);
        Path packageDirectory = toPackageDirectory(Path.of(linkedProjectPath));
        Module module = ReadAction.compute(() -> findModule(project, packageDirectory));
        Sdk sdk = findNodeSdk(settings == null ? null : settings.getNodeSdkName(), module);
        return createExecutionSettings(sdk);
    }

    public static NpmExecutionSettings createExecutionSettings(@Nullable Sdk sdk) {
        Path home = sdk == null ? null : sdk.getHomeNioPath();
        if (home == null) {
            return new NpmExecutionSettings(DEFAULT_EXECUTABLE, null);
        }
        Path npm = NodeJSBundleType.getExePath(sdk.getPlatform(), home, "npm.cmd", DEFAULT_EXECUTABLE);
        Path node = NodeJSBundleType.getExePath(sdk);
        Path nodeDirectory = node.getParent();
        return new NpmExecutionSettings(
            Files.exists(npm) ? npm.toString() : DEFAULT_EXECUTABLE,
            nodeDirectory == null ? null : nodeDirectory.toString()
        );
    }

    /**
     * The bundle named in the settings, else the bundle of the module, else the first Node.js bundle
     */
    public static @Nullable Sdk findNodeSdk(@Nullable String sdkName, @Nullable Module module) {
        Sdk sdk = sdkName == null ? null : SdkTable.getInstance().findSdk(sdkName);
        if (sdk == null && module != null) {
            NodeJSModuleExtension extension = module.getExtension(NodeJSModuleExtension.class);
            sdk = extension == null ? null : extension.getSdk();
        }
        if (sdk == null) {
            sdk = ContainerUtil.getFirstItem(SdkTable.getInstance().getSdksOfType(NodeJSBundleType.getInstance()));
        }
        return sdk;
    }

    /**
     * {@code npm <arguments>} in the package directory, with the {@code node} of the bundle first in {@code PATH}
     */
    public static GeneralCommandLine createCommandLine(NpmExecutionSettings settings, Path workDirectory, List<String> arguments) {
        List<String> command = new ArrayList<>();
        command.add(settings.getNpmExecutable());
        command.addAll(arguments);
        GeneralCommandLine commandLine = new GeneralCommandLine(command);
        commandLine.withWorkingDirectory(workDirectory);
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE);
        String nodeDirectory = settings.getNodeDirectory();
        if (nodeDirectory != null) {
            String path = commandLine.getParentEnvironment().get("PATH");
            commandLine.withEnvironment("PATH", nodeDirectory + File.pathSeparator + (path == null ? "" : path));
        }
        return commandLine;
    }

    /**
     * The npm arguments of a task: a command ({@code install}) or a script ({@code run build})
     */
    public static List<String> toArguments(String taskName) {
        List<String> arguments = new ArrayList<>();
        if (NpmConstants.COMMAND_TASKS.contains(taskName)) {
            arguments.add(taskName);
        }
        else {
            arguments.add("run");
            arguments.add(taskName);
        }
        return arguments;
    }

    public static String toPresentation(List<String> arguments) {
        return DEFAULT_EXECUTABLE + " " + String.join(" ", arguments);
    }
}
