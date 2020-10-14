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

import com.github.kklisura.cdt.protocol.commands.Debugger;
import com.github.kklisura.cdt.protocol.commands.Runtime;
import com.github.kklisura.cdt.services.ChromeDevToolsService;
import com.github.kklisura.cdt.services.WebSocketService;
import com.github.kklisura.cdt.services.config.ChromeDevToolsServiceConfiguration;
import com.github.kklisura.cdt.services.exceptions.WebSocketServiceException;
import com.github.kklisura.cdt.services.impl.ChromeDevToolsServiceImpl;
import com.github.kklisura.cdt.services.impl.ChromeServiceImpl;
import com.github.kklisura.cdt.services.impl.WebSocketServiceImpl;
import com.github.kklisura.cdt.services.invocation.CommandInvocationHandler;
import com.github.kklisura.cdt.services.types.ChromeTab;
import com.github.kklisura.cdt.services.types.ChromeVersion;
import com.github.kklisura.cdt.services.utils.ProxyUtils;
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
import com.intellij.openapi.application.Application;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.net.NetUtils;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import consulo.javascript.run.debug.V8BaseDebugProcess;
import consulo.javascript.run.debug.V8DebugProcess;
import consulo.nodejs.run.vm.WebSocketImpl;
import consulo.nodejs.run.vm.WebSocketV8DebugProcess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.dataholder.Key;
import consulo.util.lang.ref.SimpleReference;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

			final SimpleReference<V8BaseDebugProcess<?>> vm = SimpleReference.create(null);

			final XDebugSession debugSession = XDebuggerManager.getInstance(env.getProject()).startSession(env, session ->
			{
				nodeJSRunState.addProcessListener(new ProcessAdapter()
				{
					@Override
					public void onTextAvailable(ProcessEvent event, Key outputType)
					{
						if(outputType == ProcessOutputTypes.STDERR)
						{
							V8BaseDebugProcess<?> debugProcess = vm.get();
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
							else if(debugProcess instanceof WebSocketV8DebugProcess)
							{
								String prefix = "Debugger listening on ws://";
								if(StringUtil.startsWith(text, prefix))
								{
									try
									{
										String u = text.substring(prefix.length() - 5, text.length()).trim();
										URI url = new URI(u);

										Application.get().executeOnPooledThread(() ->
										{
											try
											{
												ChromeServiceImpl service = new ChromeServiceImpl("localhost", availableSocketPort, wsUrl -> new WebSocketImpl(wsUrl));

												ChromeVersion version = service.getVersion();

												List<ChromeTab> tabs = service.getTabs();


												ChromeDevToolsService devToolsService = service.createDevToolsService(tabs.get(0));

												Debugger debugger = devToolsService.getDebugger();

												debugger.onPaused(event1 ->
												{
													System.out.println(event1);
												});
												Runtime runtime = devToolsService.getRuntime();
												runtime.runIfWaitingForDebugger();

												devToolsService.close();
											}
											catch(Throwable e)
											{
												e.printStackTrace();
											}
										});
										
										vm.set(null);
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
				final V8BaseDebugProcess<?> debugProcess = isInspectBrk ? new WebSocketV8DebugProcess(session, result, availableSocketPort) : new V8DebugProcess(session, result, availableSocketPort);
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

	private static ChromeDevToolsServiceImpl connect(String url)
	{
		URI uri = null;
		try
		{
			uri = new URI(url);
		}
		catch(URISyntaxException e)
		{
			e.printStackTrace();
		}

		WebSocketService webSocketService = null;
		try
		{
			webSocketService = WebSocketServiceImpl.create(uri);
		}
		catch(WebSocketServiceException e)
		{
			e.printStackTrace();
		}
		// Create invocation handler
		CommandInvocationHandler commandInvocationHandler = new CommandInvocationHandler();

		// Setup command cache for this session
		Map<Method, Object> commandsCache = new ConcurrentHashMap<>();

		// Create dev tools service.
		ChromeDevToolsServiceImpl chromeDevToolsService =
				ProxyUtils.createProxyFromAbstract(
						ChromeDevToolsServiceImpl.class,
						new Class[]{
								WebSocketService.class,
								ChromeDevToolsServiceConfiguration.class
						},
						new Object[]{
								webSocketService,
								new ChromeDevToolsServiceConfiguration()
						},
						(unused, method, args) ->
								commandsCache.computeIfAbsent(
										method,
										key -> {
											Class<?> returnType = method.getReturnType();
											return ProxyUtils.createProxy(returnType, commandInvocationHandler);
										}));

		// Register dev tools service with invocation handler.
		commandInvocationHandler.setChromeDevToolsService(chromeDevToolsService);

		return chromeDevToolsService;
	}

	@Override
	public boolean canRun(@Nonnull String s, @Nonnull RunProfile runProfile)
	{
		return s.equals(DefaultDebugExecutor.EXECUTOR_ID) && runProfile instanceof NodeJSConfigurationBase;
	}
}