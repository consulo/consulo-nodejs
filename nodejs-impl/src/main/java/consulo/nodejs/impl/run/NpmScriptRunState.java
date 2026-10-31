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
package consulo.nodejs.impl.run;

import consulo.content.bundle.Sdk;
import consulo.module.Module;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.nodejs.run.NodeJSConfigurationBase;
import consulo.nodejs.run.NodeJSRunState;
import consulo.platform.Platform;
import consulo.process.cmd.GeneralCommandLine;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * {@code npm run <script>} with the npm of the bundle; the directory of the bundle's node comes first in the
 * {@code PATH} (npm starts node through {@code #!/usr/bin/env node}).
 */
public class NpmScriptRunState extends NodeJSRunState {
    public NpmScriptRunState(@Nullable Module module, Sdk sdk, NodeJSConfigurationBase configuration) {
        super(module, sdk, configuration);
    }

    @Override
    protected void setupExePath(GeneralCommandLine commandLine, Sdk sdk, Platform platform) {
        commandLine.withExecutablePath(NodeJSBundleType.getExePath(platform, sdk.getHomeNioPath(), "npm.cmd", "npm"));
        Path nodeDirectory = NodeJSBundleType.getExePath(sdk).getParent();
        if (nodeDirectory != null) {
            String path = platform.os().getEnvironmentVariable("PATH");
            commandLine.withEnvironment("PATH", nodeDirectory + File.pathSeparator + (path == null ? "" : path));
        }
    }
}
