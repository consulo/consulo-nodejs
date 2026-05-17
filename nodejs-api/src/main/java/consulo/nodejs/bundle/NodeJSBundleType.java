/*
 * Copyright 2013-2017 consulo.io
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

package consulo.nodejs.bundle;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.container.plugin.PluginManager;
import consulo.content.OrderRootType;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.BundleType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.content.bundle.SdkType;
import consulo.javascript.language.JavaScriptFileType;
import consulo.logging.Logger;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 14.03.14
 */
@ExtensionImpl
public class NodeJSBundleType extends BundleType {
    private static final Logger LOG = Logger.getInstance(NodeJSBundleType.class);

    @Nonnull
    public static NodeJSBundleType getInstance() {
        return Application.get().getExtensionPoint(SdkType.class).findExtensionOrFail(NodeJSBundleType.class);
    }

    @Nonnull
    public static Path getExePath(@Nonnull Sdk sdk) {
        return getExePath(sdk.getPlatform(), sdk.getHomeNioPath());
    }

    @Nonnull
    public static Path getExePath(@Nonnull Platform platform, @Nonnull Path home) {
        return getExePath(platform, home, "node.exe", "node");
    }

    @Nonnull
    public static Path getExePath(@Nonnull Platform platform, @Nonnull Path home, @Nonnull String winName, String otherName) {
        String executable = platform.os().isWindows() ? winName : otherName;

        Path firstTry = home.resolve("bin/" + executable);
        if (Files.exists(firstTry)) {
            return firstTry;
        }
        return home.resolve(executable);
    }

    public NodeJSBundleType() {
        super("NODEJS", NodeJSLocalize.nodejsName(), NodeJSApiIconGroup.nodejs());
    }

    @Override
    public void collectHomePaths(Platform platform, Consumer<Path> consumer) {
        if (platform.os().isWindows()) {
            collectNodePathsAtWindows(platform, consumer, "ProgramFiles");
            collectNodePathsAtWindows(platform, consumer, "ProgramFiles(x86)");
        }
        else {
            Path usrBinNode = platform.fs().getPath("/usr/bin/node");
            if (Files.exists(usrBinNode)) {
                consumer.accept(usrBinNode.getParent());
            }

            Path userHome = platform.user().homePath();

            Path nvmNodeDir = userHome.resolve(".nvm/versions/node");
            if (Files.exists(nvmNodeDir)) {
                try {
                    Files.list(nvmNodeDir).forEach(path -> {
                        if (Files.isDirectory(path)) {
                            consumer.accept(path);
                        }
                    });
                }
                catch (IOException e) {
                    LOG.warn("Fail to visit dir " + nvmNodeDir, e);
                }
            }
        }
    }

    private static void collectNodePathsAtWindows(Platform platform, Consumer<Path> pathConsumer, String env) {
        String programFiles = platform.os().getEnvironmentVariable(env);
        if (programFiles == null) {
            return;
        }

        Path programFilesPath = platform.fs().getPath(programFiles);

        Path nodejsPath = programFilesPath.resolve("nodejs");
        if (Files.exists(nodejsPath)) {
            pathConsumer.accept(nodejsPath);
        }
    }

    @Override
    public void setupSdkPaths(Sdk sdk) {
        SdkModificator modificator = sdk.getSdkModificator();

        VirtualFile stubsDirectory = LocalFileSystem.getInstance().findFileByIoFile(new File(PluginManager.getPluginPath(NodeJSBundleType.class), "stubs"));
        if (stubsDirectory != null) {
            for (VirtualFile file : stubsDirectory.getChildren()) {
                if (file.getFileType() == JavaScriptFileType.INSTANCE) {
                    modificator.addRoot(file, BinariesOrderRootType.getInstance());
                    modificator.addRoot(file, SourcesOrderRootType.getInstance());
                }
            }
        }

        modificator.commitChanges();
    }

    @Override
    public boolean canCreatePredefinedSdks(Platform platform) {
        return Platform.LOCAL.equals(platform.getId());
    }

    @Override
    public boolean isValidSdkHome(Platform platform, Path path) {
        return Files.exists(getExePath(platform, path));
    }

    @Override
    public @Nullable String getVersionString(Platform platform, Path path) {
        Path exePath = getExePath(platform, path);

        try {
            GeneralCommandLine commandLine = new GeneralCommandLine();
            commandLine.withExecutablePath(exePath);
            commandLine.withWorkingDirectory(path);
            commandLine.addParameter("-v");
            commandLine.withPlatform(platform);

            ProcessOutput processOutput = CapturingProcessUtil.execAndGetOutput(commandLine);
            String stdout = processOutput.getStdout();
            if (StringUtil.startsWith(stdout, "v")) {
                stdout = stdout.substring(1, stdout.length());
            }
            return stdout.trim();
        }
        catch (ExecutionException e) {
            LOG.warn("Failed to execute " + exePath, e);
            return null;
        }
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type) {
        return type == BinariesOrderRootType.getInstance() || type == SourcesOrderRootType.getInstance();
    }
}
