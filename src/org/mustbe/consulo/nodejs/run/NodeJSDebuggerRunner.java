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

package org.mustbe.consulo.nodejs.run;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.nodejs.run.debug.V8DebugProcess;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;

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
	protected RunContentDescriptor doExecute(final Project project, final RunProfileState state, final RunContentDescriptor contentToReuse,
			final ExecutionEnvironment env) throws ExecutionException
	{
		FileDocumentManager.getInstance().saveAllDocuments();
		try
		{
			final int availableSocketPort = NetUtils.findAvailableSocketPort();
			NodeJSRunState nodeJSRunState = (NodeJSRunState) state;
			nodeJSRunState.addArgument("--debug-brk=" + availableSocketPort);

			final XDebugSession debugSession = XDebuggerManager.getInstance(project).startSession(env, new XDebugProcessStarter()
			{
				@NotNull
				@Override
				public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException
				{
					final ExecutionResult result = state.execute(env.getExecutor(), NodeJSDebuggerRunner.this);
					return new V8DebugProcess(session, result, availableSocketPort);
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
		return s.equals(DefaultDebugExecutor.EXECUTOR_ID) && runProfile instanceof NodeJSConfiguration;
	}
}
