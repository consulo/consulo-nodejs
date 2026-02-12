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
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.content.bundle.SdkType;
import consulo.javascript.language.JavaScriptFileType;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 14.03.14
 */
@ExtensionImpl
public class NodeJSBundleType extends SdkType {
    @Nonnull
    public static NodeJSBundleType getInstance() {
        return Application.get().getExtensionPoint(SdkType.class).findExtensionOrFail(NodeJSBundleType.class);
    }

    @Nonnull
    public static File getExePath(@Nonnull Sdk sdk) {
        String homePath = sdk.getHomePath();
        assert homePath != null;
        return getExePath(homePath);
    }

    @Nonnull
    public static File getExePath(@Nonnull String home) {
        return getExePath(home, "node.exe", "node");
    }

    @Nonnull
    public static File getExePath(@Nonnull String home, @Nonnull String winName, String otherName) {
        String executable = Platform.current().os().isWindows() ? winName : otherName;

        File firstTry = new File(home, "bin/" + executable);
        if (firstTry.exists()) {
            return firstTry;
        }
        return new File(home, executable);
    }

    public NodeJSBundleType() {
        super("NODEJS", NodeJSLocalize.nodejsName(), NodeJSApiIconGroup.nodejs());
    }

    @Nonnull
    @Override
    public Collection<String> suggestHomePaths() {
        List<String> paths = new ArrayList<>();
        Platform platform = Platform.current();
        if (platform.os().isWindows()) {
            collectNodePathsAtWindows(paths, "ProgramFiles");
            collectNodePathsAtWindows(paths, "ProgramFiles(x86)");
        }
        else {
            File userHome = platform.user().homePath().toFile();

            File nvmHome = new File(userHome, ".nvm/versions/node");
            if (nvmHome.exists()) {
                for (File file : nvmHome.listFiles()) {
                    if (file.isDirectory()) {
                        paths.add(file.getPath());
                    }
                }
            }
        }
        return paths;
    }

    private static void collectNodePathsAtWindows(List<String> list, String env) {
        String programFiles = Platform.current().os().getEnvironmentVariable(env);
        if (programFiles != null) {
            File nodejsPath = new File(programFiles, "nodejs");
            if (nodejsPath.exists()) {
                list.add(nodejsPath.getPath());
            }
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
    public boolean canCreatePredefinedSdks() {
        return true;
    }

    @Override
    public boolean isValidSdkHome(String sdkHome) {
        return getExePath(sdkHome).exists();
    }

    @Nullable
    @Override
    public String getVersionString(String s) {
        try {
            GeneralCommandLine commandLine = new GeneralCommandLine();
            commandLine.setExePath(getExePath(s).getPath());
            commandLine.withWorkDirectory(s);
            commandLine.addParameter("-v");

            ProcessOutput processOutput = CapturingProcessUtil.execAndGetOutput(commandLine);
            String stdout = processOutput.getStdout();
            if (StringUtil.startsWith(stdout, "v")) {
                stdout = stdout.substring(1, stdout.length());
            }
            return stdout.trim();
        }
        catch (ExecutionException e) {
            return null;
        }
    }

    @Override
    public boolean isRootTypeApplicable(OrderRootType type) {
        return type == BinariesOrderRootType.getInstance() || type == SourcesOrderRootType.getInstance();
    }

    @Override
    public String suggestSdkName(String s, String sdkHome) {
        return "nodejs " + getVersionString(sdkHome);
    }
}
