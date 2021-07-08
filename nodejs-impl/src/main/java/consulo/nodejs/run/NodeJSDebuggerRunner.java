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

package consulo.nodejs.run;

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
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import consulo.javascript.run.debug.V8DebugProcess;
import consulo.javascript.run.debug.inspect.InspectDebugProcess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.dataholder.Key;
import consulo.util.lang.ref.SimpleReference;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class NodeJSDebuggerRunner extends DefaultProgramRunner
{
	@Nonnull
	@Override
	public String getRunnerId()
	{
		return "NodeJSDebuggerRunner";
	}

	@Override
	@RequiredUIAccess
	protected RunContentDescriptor doExecute(@Nonnull final RunProfileState state, @Nonnull final ExecutionEnvironment env) throws ExecutionException
	{
		FileDocumentManager.getInstance().saveAllDocuments();
		try
		{
			final int availableSocketPort = NetUtils.findAvailableSocketPort();
			final NodeJSRunState nodeJSRunState = (NodeJSRunState) state;

			String versionString = ((NodeJSRunState) state).getSdk().getVersionString();

			boolean isInspectBrk = StringUtil.compareVersionNumbers(versionString, "7.0.0") > 0;
			if(isInspectBrk)
			{
				nodeJSRunState.addVmArgument("--inspect-brk=" + availableSocketPort);
			}
			else
			{
				nodeJSRunState.addVmArgument("--debug-brk=" + availableSocketPort);
			}

			final SimpleReference<XDebugProcess> vm = SimpleReference.create(null);

			final XDebugSession debugSession = XDebuggerManager.getInstance(env.getProject()).startSession(env, session ->
			{
				nodeJSRunState.addProcessListener(new ProcessAdapter()
				{
					@Override
					public void onTextAvailable(ProcessEvent event, Key outputType)
					{
						if(outputType == ProcessOutputTypes.STDERR)
						{
							XDebugProcess debugProcess = vm.get();
							if(debugProcess == null)
							{
								return;
							}

							String text = event.getText();
							if(debugProcess instanceof V8DebugProcess)
							{
								if(StringUtil.startsWith(text, "Debugger listening on port"))
								{
									try
									{
										((V8DebugProcess) debugProcess).attach();
										vm.set(null);
									}
									catch(Exception e)
									{
										session.getConsoleView().print(ExceptionUtil.getThrowableText(e), ConsoleViewContentType.ERROR_OUTPUT);
									}
								}
							}
							else if(debugProcess instanceof InspectDebugProcess)
							{
								String prefix = "Debugger listening on ws://";
								if(StringUtil.startsWith(text, prefix))
								{
									try
									{
										((InspectDebugProcess) debugProcess).attach();
									}
									catch(Exception e)
									{
										session.getConsoleView().print(ExceptionUtil.getThrowableText(e), ConsoleViewContentType.ERROR_OUTPUT);
									}
								}

								if(StringUtil.startsWith(text, "Waiting for the debugger to disconnect"))
								{
									try
									{
										((InspectDebugProcess) debugProcess).disconnect();
									}
									catch(Exception e)
									{
										session.getConsoleView().print(ExceptionUtil.getThrowableText(e), ConsoleViewContentType.ERROR_OUTPUT);
									}
								}
							}
						}
					}
				});

				final ExecutionResult result = state.execute(env.getExecutor(), NodeJSDebuggerRunner.this);
				final XDebugProcess debugProcess = isInspectBrk ? new InspectDebugProcess(session, result, availableSocketPort) : new V8DebugProcess(session, result, availableSocketPort);
				vm.set(debugProcess);

				return debugProcess;
			});

			return debugSession.getRunContentDescriptor();
		}
		catch(IOException e)
		{
			throw new ExecutionException(e);
		}
	}

	@Override
	public boolean canRun(@Nonnull String s, @Nonnull RunProfile runProfile)
	{
		return s.equals(DefaultDebugExecutor.EXECUTOR_ID) && runProfile instanceof NodeJSConfigurationBase;
	}
}