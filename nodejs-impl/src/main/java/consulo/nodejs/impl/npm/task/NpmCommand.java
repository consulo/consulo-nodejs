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
import consulo.nodejs.impl.npm.NpmUtil;
import consulo.nodejs.impl.npm.setting.NpmExecutionSettings;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.dataholder.Key;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Runs {@code npm <arguments>} in a package directory, the output going to the task listener.
 *
 * @since 2026-10-31
 */
public class NpmCommand {
    private volatile @Nullable ProcessHandler myProcessHandler;
    private volatile boolean myCancelled;

    public void run(ExternalSystemTaskId id,
                    NpmExecutionSettings settings,
                    Path packageDirectory,
                    List<String> arguments,
                    ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        GeneralCommandLine commandLine = NpmUtil.createCommandLine(settings, packageDirectory, arguments);
        String presentation = NpmUtil.toPresentation(arguments);
        listener.onTaskOutput(id, "> " + presentation + "\n", true);

        ProcessHandler processHandler;
        try {
            processHandler = ProcessHandlerBuilder.create(commandLine).killable().build();
        }
        catch (ExecutionException e) {
            throw new ExternalSystemException(NodeJSLocalize.npmErrorCannotRun(presentation, String.valueOf(e.getMessage())).get());
        }

        AtomicInteger exitCode = new AtomicInteger(-1);
        processHandler.addProcessListener(new ProcessListener() {
            @Override
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (outputType == ProcessOutputTypes.STDOUT) {
                    listener.onTaskOutput(id, event.getText(), true);
                }
                else if (outputType == ProcessOutputTypes.STDERR) {
                    listener.onTaskOutput(id, event.getText(), false);
                }
            }

            @Override
            public void processTerminated(ProcessEvent event) {
                exitCode.set(event.getExitCode());
            }
        });

        myProcessHandler = processHandler;
        try {
            processHandler.startNotify();
            processHandler.waitFor();
        }
        finally {
            myProcessHandler = null;
        }

        if (myCancelled) {
            throw new ExternalSystemException(NodeJSLocalize.npmErrorCancelled(presentation).get());
        }
        int code = exitCode.get();
        if (code != 0) {
            throw new ExternalSystemException(NodeJSLocalize.npmErrorCommandFailed(presentation, code).get());
        }
    }

    public boolean cancel() {
        myCancelled = true;
        ProcessHandler processHandler = myProcessHandler;
        if (processHandler == null) {
            return false;
        }
        processHandler.destroyProcess();
        return true;
    }
}
