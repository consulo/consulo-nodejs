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

package consulo.nodejs.impl.run;

import com.github.kklisura.cdt.services.impl.ChromeDevToolsServiceImpl;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.document.FileDocumentManager;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.*;
import consulo.execution.runner.DefaultProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.RunContentDescriptor;
import consulo.execution.ui.console.ConsoleViewContentType;
import consulo.javascript.debugger.cdt.ChromeDevToolsFactory;
import consulo.nodejs.run.NodeJSConfigurationBase;
import consulo.nodejs.run.NodeJSRunState;
import consulo.process.ExecutionException;
import consulo.process.ProcessOutputTypes;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.concurrent.CancellablePromise;
import consulo.util.dataholder.Key;
import consulo.util.io.NetUtil;
import consulo.util.lang.ExceptionUtil;
import consulo.util.lang.ref.SimpleReference;
import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author VISTALL
 * @since 20.03.14
 */
@ExtensionImpl
public class NodeJSDebuggerRunner extends DefaultProgramRunner {
    private static final Pattern CONNECT_PATTERN = Pattern.compile("wss?://(?:\\[[0-9a-fA-F:]+\\]|[^/\\s:]+):\\d+/[^/\\s]+");

    @Nonnull
    @Override
    public String getRunnerId() {
        return "NodeJSDebuggerRunner";
    }

    @Override
    @RequiredUIAccess
    protected RunContentDescriptor doExecute(@Nonnull final RunProfileState state, @Nonnull final ExecutionEnvironment env) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();
        try {
            final int wsPort = NetUtil.findAvailableSocketPort();
            final NodeJSRunState nodeJSRunState = (NodeJSRunState) state;
            nodeJSRunState.addVmArgument("--inspect-brk=" + wsPort);

            final SimpleReference<NodeJSDebugProcess> vm = SimpleReference.create(null);

            final XDebugSession debugSession = XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {
                @Nonnull
                @Override
                public XDebugProcess start(@Nonnull final XDebugSession session) throws ExecutionException {
                    nodeJSRunState.addProcessListener(new ProcessListener() {
                        @Override
                        public void onTextAvailable(ProcessEvent event, Key outputType) {
                            if (outputType == ProcessOutputTypes.STDERR) {
                                NodeJSDebugProcess debugProcess = vm.get();
                                if (debugProcess == null) {
                                    return;
                                }

                                Matcher matcher = CONNECT_PATTERN.matcher(event.getText());
                                if (matcher.find()) {
                                    try {
                                        String url = matcher.group();

                                        ChromeDevToolsServiceImpl tools = ChromeDevToolsFactory.create(new NodeJSWebSocketConnection(url));

                                        tools.getRuntime().enable();

                                        tools.getDebugger().enable();

                                        debugProcess.initTools(tools);

                                        CancellablePromise<Void> promise =
                                            ReadAction.nonBlocking(() -> debugProcess.getSession().initBreakpoints()).submitDefault();

                                        promise.onProcessed(unused -> tools.getRuntime().runIfWaitingForDebugger());

                                        vm.set(null);
                                    }
                                    catch (Exception e) {
                                        session.getConsoleView().print(ExceptionUtil.getThrowableText(e), ConsoleViewContentType.ERROR_OUTPUT);
                                    }

                                    // remove this listener from handling
                                    debugProcess.getProcessHandler().removeProcessListener(this);
                                }
                            }
                        }
                    });

                    ExecutionResult result = state.execute(env.getExecutor(), NodeJSDebuggerRunner.this);

                    NodeJSDebugProcess debugProcess = new NodeJSDebugProcess(session, result);

                    vm.set(debugProcess);

                    return debugProcess;
                }
            });
            return debugSession.getRunContentDescriptor();
        }
        catch (IOException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public boolean canRun(@Nonnull String s, @Nonnull RunProfile runProfile) {
        return s.equals(DefaultDebugExecutor.EXECUTOR_ID) && runProfile instanceof NodeJSConfigurationBase;
    }
}