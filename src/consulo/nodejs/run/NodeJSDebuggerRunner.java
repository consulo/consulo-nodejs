/*
 * Copyright 2013-2014 must-be.org
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

package consulo.nodejs.run;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import consulo.annotations.RequiredDispatchThread;
import consulo.javascript.run.debug.V8DebugProcess;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class NodeJSDebuggerRunner extends DefaultProgramRunner
{
	@NotNull
	@Override
	public String getRunnerId()
	{
		return "NodeJSDebuggerRunner";
	}

	@Override
	@RequiredDispatchThread
	protected RunContentDescriptor doExecute(@NotNull final RunProfileState state, @NotNull final ExecutionEnvironment env) throws ExecutionException
	{
		FileDocumentManager.getInstance().saveAllDocuments();
		try
		{
			final int availableSocketPort = NetUtils.findAvailableSocketPort();
			final NodeJSRunState nodeJSRunState = (NodeJSRunState) state;
			nodeJSRunState.addVmArgument("--debug-brk=" + availableSocketPort);

			final Ref<V8DebugProcess> vm = Ref.create(null);

			final XDebugSession debugSession = XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter()
			{
				@NotNull
				@Override
				public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException
				{
					nodeJSRunState.addProcessListener(new ProcessAdapter()
					{
						@Override
						public void onTextAvailable(ProcessEvent event, Key outputType)
						{
							if(outputType == ProcessOutputTypes.STDERR)
							{
								V8DebugProcess debugProcess = vm.get();
								if(debugProcess == null)
								{
									return;
								}

								if(StringUtil.startsWith(event.getText(), "Debugger listening on port"))
								{
									try
									{
										debugProcess.attach();
										vm.set(null);
									}
									catch(Exception e)
									{
										session.getConsoleView().print(ExceptionUtil.getThrowableText(e), ConsoleViewContentType.ERROR_OUTPUT);
									}
								}
							}
						}
					});

					final ExecutionResult result = state.execute(env.getExecutor(), NodeJSDebuggerRunner.this);
					final V8DebugProcess debugProcess = new V8DebugProcess(session, result, availableSocketPort);
					vm.set(debugProcess);

					return debugProcess;
				}
			});
			return debugSession.getRunContentDescriptor();
		}
		catch(IOException e)
		{
			throw new ExecutionException(e);
		}
	}

	@Override
	public boolean canRun(@NotNull String s, @NotNull RunProfile runProfile)
	{
		return s.equals(DefaultDebugExecutor.EXECUTOR_ID) && runProfile instanceof NodeJSConfigurationBase;
	}
}