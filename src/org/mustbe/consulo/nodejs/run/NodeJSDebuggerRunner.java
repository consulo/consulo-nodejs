package org.mustbe.consulo.nodejs.run;

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
		NodeJSRunState nodeJSRunState = (NodeJSRunState) state;
		nodeJSRunState.addArgument("--debug-brk=9555");

		final XDebugSession debugSession = XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter()
		{
			@NotNull
			@Override
			public XDebugProcess start(@NotNull XDebugSession session) throws ExecutionException
			{
				final ExecutionResult result = state.execute(env.getExecutor(), NodeJSDebuggerRunner.this);
				return new V8DebugProcess(session, result, 9555);
			}
		});
		return debugSession.getRunContentDescriptor();
	}

	@Override
	public boolean canRun(@NotNull String s, @NotNull RunProfile runProfile)
	{
		return s.equals(DefaultDebugExecutor.EXECUTOR_ID) && runProfile instanceof NodeJSConfiguration;
	}
}
