package consulo.javascript.run.debug.inspect;

import com.github.kklisura.cdt.protocol.commands.Debugger;
import com.github.kklisura.cdt.protocol.commands.Runtime;
import com.github.kklisura.cdt.protocol.events.debugger.ScriptParsed;
import com.github.kklisura.cdt.protocol.types.debugger.SetBreakpointByUrl;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.impl.ChromeServiceImpl;
import com.github.kklisura.cdt.services.types.ChromeTab;
import com.google.common.base.Objects;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.content.Content;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XExpression;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.ui.XDebugTabLayouter;
import consulo.application.AccessRule;
import consulo.javascript.debugger.JavaScriptEditorsProvider;
import consulo.javascript.debugger.JavaScriptLineBreakpointType;
import consulo.javascript.debugger.JavaScriptListPanel;
import consulo.javascript.icon.JavaScriptIconGroup;
import consulo.logging.Logger;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 30/01/2021
 */
public class InspectDebugProcess extends XDebugProcess
{
	private static final Logger LOG = Logger.getInstance(InspectDebugProcess.class);

	private final ExecutionResult myExecutionResult;
	private final int myPort;
	private final XBreakpointManager myXBreakpointManager;

	private ChromeDevToolsService myChromeDevToolsService;
	private Debugger myChromeDebugger;

	private JavaScriptListPanel<ScriptParsed> myScriptListPanel;

	public InspectDebugProcess(@Nonnull XDebugSession session, ExecutionResult executionResult, int port)
	{
		super(session);
		myExecutionResult = executionResult;
		myPort = port;

		myXBreakpointManager = XDebuggerManager.getInstance(getSession().getProject()).getBreakpointManager();

		myScriptListPanel = new JavaScriptListPanel<ScriptParsed>(session.getProject())
		{
			@Nullable
			@Override
			public VirtualFile toVirtualFile(@Nonnull ScriptParsed value, boolean toOpen)
			{
				return InspectDebugProcess.toVirtualFile(value, toOpen);
			}
		};
		getSession().setPauseActionSupported(true);
	}

	private static final Key<Object> SOME_VALUE_TO_EQUALS = Key.create("some-value-to-equals");

	@Nonnull
	public static VirtualFile toVirtualFile(@Nonnull ScriptParsed value, boolean toOpen)
	{
		String scriptUrl = value.getUrl();
		assert scriptUrl != null;
		VirtualFile file = null;
		try
		{
			file = VfsUtil.findFileByURL(new URL(scriptUrl));
		}
		catch(MalformedURLException ignored)
		{
		}

		if(file != null)
		{
			return file;
		}

		return createLightFile(scriptUrl);
	}

	public static VirtualFile createLightFile(String scriptUrl)
	{
		String extension = FileUtil.getExtension(scriptUrl);
		LightVirtualFile virtualFile = new LightVirtualFile(scriptUrl, FileTypeRegistry.getInstance().getFileTypeByExtension(extension), "")
		{
			@Override
			public boolean equals(Object obj)
			{
				if(obj instanceof LightVirtualFile)
				{
					if(!Objects.equal(getPath(), ((LightVirtualFile) obj).getPath()))
					{
						return false;
					}
					Object userData = ((LightVirtualFile) obj).getUserData(SOME_VALUE_TO_EQUALS);
					Object userData1 = getUserData(SOME_VALUE_TO_EQUALS);
					return Objects.equal(userData1, userData);
				}
				return false;
			}
		};
		virtualFile.putUserData(SOME_VALUE_TO_EQUALS, scriptUrl);
		return virtualFile;
	}

	public void attach()
	{
		ChromeServiceImpl service = new ChromeServiceImpl("localhost", myPort, WebSocketServiceImplOverJavaWebSocket::new);

		List<ChromeTab> tabs = service.getTabs();

		ChromeTab chromeTab = tabs.get(0);

		myChromeDevToolsService = service.createDevToolsService(chromeTab);

		myChromeDebugger = myChromeDevToolsService.getDebugger();
		String enable = myChromeDebugger.enable();

		myChromeDebugger.onPaused(pauseEvent ->
		{
			String reason = pauseEvent.getReason();

			if("Break on start".equals(reason))
			{
				Application.get().getLastUIAccess().give(() -> {
					getSession().initBreakpoints();

					getSession().positionReached(new InspectSuspendContext(pauseEvent));
				});

			}
			else
			{
				System.out.println();
			}
		});

		myChromeDebugger.onScriptParsed(scriptParsed -> onScriptParsed(scriptParsed));

		Runtime runtime = myChromeDevToolsService.getRuntime();

		runtime.runIfWaitingForDebugger();
	}

	protected void onScriptParsed(ScriptParsed scriptParsed)
	{
		myScriptListPanel.add(scriptParsed);
	}

	public void disconnect()
	{
		if(myChromeDevToolsService != null)
		{
			myChromeDevToolsService.close();
			myChromeDevToolsService = null;
		}
	}

	protected void insertBreakpont(@Nonnull XLineBreakpoint<?> lineBreakpoint)
	{
		String presentableFilePath = lineBreakpoint.getPresentableFilePath();
		int line = lineBreakpoint.getLine();
		XExpression conditionExpression = lineBreakpoint.getConditionExpression();
		String expression = conditionExpression == null ? null : conditionExpression.getExpression();

		try
		{
			Path path = Paths.get(presentableFilePath);

			String uri = path.toUri().toString();

			SetBreakpointByUrl setBreakpoint = myChromeDebugger.setBreakpointByUrl(line, uri, null, null, null, expression);

			if(setBreakpoint.getLocations().isEmpty())
			{
				myXBreakpointManager.updateBreakpointPresentation(lineBreakpoint, AllIcons.Debugger.Db_invalid_breakpoint, "Can't find location");
			}
			else
			{
				myXBreakpointManager.updateBreakpointPresentation(lineBreakpoint, AllIcons.Debugger.Db_verified_breakpoint, null);
			}
		}
		catch(Throwable e)
		{
			myXBreakpointManager.updateBreakpointPresentation(lineBreakpoint, AllIcons.Debugger.Db_invalid_breakpoint, e.getMessage());

			LOG.warn(e);
		}
	}

	@Nonnull
	@Override
	public XBreakpointHandler<?>[] getBreakpointHandlers()
	{
		return new XBreakpointHandler[]{
				new XBreakpointHandler<XLineBreakpoint<XBreakpointProperties>>(JavaScriptLineBreakpointType.class)
				{
					@Override
					public void registerBreakpoint(@Nonnull final XLineBreakpoint lineBreakpoint)
					{
						insertBreakpont(lineBreakpoint);


						//						myVm.setBreakpoint(new Breakpoint.Target.ScriptName(presentableFilePath), line, Breakpoint.EMPTY_VALUE, true, expression, new JavascriptVm
						// .BreakpointCallback()
						//						{
						//							@Override
						//							public void success(Breakpoint breakpoint)
						//							{
						//								myBreakpoints.put(breakpoint, lineBreakpoint);
						//								myXBreakpointManager.updateBreakpointPresentation(lineBreakpoint, AllIcons.Debugger.Db_verified_breakpoint, null);
						//							}
						//
						//							@Override
						//							public void failure(String s)
						//							{
						//								myXBreakpointManager.updateBreakpointPresentation(lineBreakpoint, AllIcons.Debugger.Db_invalid_breakpoint, s);
						//							}
						//						}, null);
					}

					@Override
					public void unregisterBreakpoint(@Nonnull XLineBreakpoint breakpoint, boolean b)
					{
						String presentableFilePath = breakpoint.getPresentableFilePath();
						// todo myVm.setBreakpoint(new Breakpoint.Target.ScriptName(presentableFilePath), breakpoint.getLine(), Breakpoint.EMPTY_VALUE, false, null, null, null);
					}
				}
		};
	}

	@Nonnull
	@Override
	public XDebugTabLayouter createTabLayouter()
	{
		return new XDebugTabLayouter()
		{
			@Override
			public void registerAdditionalContent(@Nonnull RunnerLayoutUi ui)
			{
				Content content = ui.createContent("ScriptListView", myScriptListPanel, "Scripts", JavaScriptIconGroup.javaScript(), null);
				content.setCloseable(false);

				ui.addContent(content);
			}
		};
	}

	@Override
	public boolean checkCanPerformCommands()
	{
		return myChromeDevToolsService != null && !myChromeDevToolsService.isClosed();
	}

	@Override
	public boolean checkCanInitBreakpoints()
	{
		return false;
	}

	@Nonnull
	@Override
	public ExecutionConsole createConsole()
	{
		return myExecutionResult.getExecutionConsole();
	}

	@Nonnull
	@Override
	public XDebuggerEditorsProvider getEditorsProvider()
	{
		return JavaScriptEditorsProvider.INSTANCE;
	}

	@Nullable
	@Override
	protected ProcessHandler doGetProcessHandler()
	{
		return myExecutionResult.getProcessHandler();
	}

	@Override
	public void stop()
	{
		disconnect();

		Application.get().runReadAction(() -> {
			Collection<? extends XLineBreakpoint<XBreakpointProperties>> breakpoints = myXBreakpointManager.getBreakpoints(JavaScriptLineBreakpointType.class);
			for(XLineBreakpoint<XBreakpointProperties> breakpoint : breakpoints)
			{
				myXBreakpointManager.updateBreakpointPresentation(breakpoint, null, null);
			}
		});
	}
}
