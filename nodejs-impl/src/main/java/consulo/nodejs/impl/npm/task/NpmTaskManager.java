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
package consulo.nodejs.impl.npm.task;

import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.nodejs.impl.npm.NpmUtil;
import consulo.nodejs.impl.npm.setting.NpmExecutionSettings;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

/**
 * The tasks of a package: the npm commands ({@code install}) and the scripts ({@code npm run <script>}).
 *
 * @since 2026-10-31
 */
public class NpmTaskManager implements ExternalSystemTaskManager<NpmExecutionSettings> {
    private final NpmCommand myCommand = new NpmCommand();

    @Override
    public void executeTasks(ExternalSystemTaskId id,
                             List<String> taskNames,
                             String projectPath,
                             @Nullable NpmExecutionSettings settings,
                             List<String> vmOptions,
                             List<String> scriptParameters,
                             @Nullable String debuggerSetup,
                             ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        NpmExecutionSettings executionSettings = settings != null ? settings : NpmUtil.createExecutionSettings(null);
        Path packageDirectory = NpmUtil.toPackageDirectory(Path.of(projectPath));
        for (String taskName : taskNames) {
            List<String> arguments = NpmUtil.toArguments(taskName);
            if (!scriptParameters.isEmpty()) {
                // the arguments of a script follow "--"
                if (arguments.size() > 1) {
                    arguments.add("--");
                }
                arguments.addAll(scriptParameters);
            }
            myCommand.run(id, executionSettings, packageDirectory, arguments, listener);
        }
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId id, ExternalSystemTaskNotificationListener listener) {
        return myCommand.cancel();
    }
}
