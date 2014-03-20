package org.mustbe.consulo.nodejs.run.debug;

import java.net.InetSocketAddress;

import org.chromium.sdk.Breakpoint;
import org.chromium.sdk.BrowserFactory;
import org.chromium.sdk.DebugContext;
import org.chromium.sdk.JavascriptVm;
import org.chromium.sdk.StandaloneVm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerBundle;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProviderBase;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class V8DebugProcess extends XDebugProcess
{
	private final ExecutionResult myResult;
	private final StandaloneVm myVm;
	private DebugContext myCurrentDebugContext;

	public V8DebugProcess(@NotNull XDebugSession session, ExecutionResult result, int port) throws ExecutionException
	{
		super(session);
		myResult = result;
		getSession().setPauseActionSupported(true);
		myVm = BrowserFactory.getInstance().createStandalone(new InetSocketAddress("localhost", port), null);
		try
		{
			myVm.attach(new V8DebugEventListener(this));
		}
		catch(Exception e)
		{
			throw new ExecutionException(e);
		}
	}

	@Nullable
	@Override
	protected ProcessHandler doGetProcessHandler()
	{
		return myResult.getProcessHandler();
	}

	@Override
	public boolean checkCanPerformCommands()
	{
		return myVm != null && myVm.isAttached();
	}

	@NotNull
	@Override
	public XDebuggerEditorsProvider getEditorsProvider()
	{
		return new XDebuggerEditorsProviderBase()
		{
			@Override
			protected PsiFile createExpressionCodeFragment(@NotNull Project project, @NotNull String s, @Nullable PsiElement element, boolean b)
			{
				return null;
			}

			@NotNull
			@Override
			public FileType getFileType()
			{
				return JavaScriptFileType.INSTANCE;
			}
		};
	}

	@NotNull
	@Override
	public ExecutionConsole createConsole()
	{
		return myResult.getExecutionConsole();
	}

	@Override
	public XBreakpointHandler<?>[] getBreakpointHandlers()
	{
		return new XBreakpointHandler[]{new XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>(JavaScriptLineBreakpointType.class)
		{
			@Override
			public void registerBreakpoint(@NotNull XLineBreakpoint xBreakpoint)
			{
				String presentableFilePath = xBreakpoint.getPresentableFilePath();
				myVm.setBreakpoint(new Breakpoint.Target.ScriptName(presentableFilePath), xBreakpoint.getLine(), 0, true, null, null, null);
			}

			@Override
			public void unregisterBreakpoint(@NotNull XLineBreakpoint xBreakpoint, boolean b)
			{
				String presentableFilePath = xBreakpoint.getPresentableFilePath();
				myVm.setBreakpoint(new Breakpoint.Target.ScriptName(presentableFilePath), xBreakpoint.getLine(), 0, false, null, null, null);
			}
		}};
	}

	@Override
	public void startPausing()
	{
		myVm.suspend(new JavascriptVm.SuspendCallback()
		{
			@Override
			public void success()
			{
				//
			}

			@Override
			public void failure(Exception e)
			{

			}
		});
	}

	@Override
	public void startStepOver()
	{

	}

	@Override
	public void startStepInto()
	{

	}

	@Override
	public void startStepOut()
	{

	}

	@Override
	public void stop()
	{
		myVm.detach();
	}

	@Override
	public void resume()
	{
		DebugContext currentDebugContext = myCurrentDebugContext;
		if(currentDebugContext == null)
		{
			return;
		}
		currentDebugContext.continueVm(DebugContext.StepAction.CONTINUE, 0, null, null);
	}

	@Override
	public void runToPosition(@NotNull XSourcePosition xSourcePosition)
	{

	}

	@Override
	public String getCurrentStateMessage()
	{
		if(myVm == null)
		{
			return XDebuggerBundle.message("debugger.state.message.disconnected");
		}
		else
		{
			if(myVm.isAttached())
			{
				return "Attached";
			}
			else
			{
				String disconnectReason = myVm.getDisconnectReason();
				if(disconnectReason == null)
				{
					return XDebuggerBundle.message("debugger.state.message.disconnected");
				}
				return "Disconnected: " + disconnectReason;
			}
		}
	}

	public void setCurrentDebugContext(DebugContext debugContext)
	{
		myCurrentDebugContext = debugContext;
	}
}
