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

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ColoredProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SmartList;
import consulo.nodejs.bundle.NodeJSBundleType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class NodeJSRunState implements RunProfileState
{
	private final Module myModule;
	private final Sdk mySdk;
	private final NodeJSConfigurationBase myConfiguration;
	private final List<String> myVmArguments = new SmartList<String>();
	private final List<String> myArguments = new SmartList<String>();
	private final List<ProcessListener> myProcessListeners = new SmartList<ProcessListener>();

	public NodeJSRunState(@Nullable Module module, @Nonnull Sdk sdk, NodeJSConfigurationBase configuration)
	{
		myModule = module;
		mySdk = sdk;
		myConfiguration = configuration;
	}

	public void addProcessListener(ProcessListener processListener)
	{
		myProcessListeners.add(processListener);
	}

	public void addArgument(String argument)
	{
		myArguments.add(argument);
	}

	public void addVmArgument(String argument)
	{
		myVmArguments.add(argument);
	}

	protected void setupExePath(@Nonnull GeneralCommandLine commandLine, @Nonnull Sdk sdk)
	{
		commandLine.setExePath(NodeJSBundleType.getExePath(mySdk).getPath());
	}

	@Nullable
	@Override
	public ExecutionResult execute(Executor executor, @Nonnull ProgramRunner programRunner) throws ExecutionException
	{
		GeneralCommandLine generalCommandLine = new GeneralCommandLine();

		String workingDirectory = myConfiguration.getWorkingDirectory();
		if(!StringUtil.isEmpty(workingDirectory))
		{
			generalCommandLine.withWorkDirectory(myConfiguration.getWorkingDirectory());
		}
		else if(myModule != null)
		{
			generalCommandLine.withWorkDirectory(myModule.getModuleDirPath());
		}

		setupExePath(generalCommandLine, mySdk);

		String vmParameters = myConfiguration.getVmParameters();
		if(!StringUtil.isEmpty(vmParameters))
		{
			generalCommandLine.addParameters(StringUtil.splitHonorQuotes(vmParameters, ' '));
		}

		generalCommandLine.addParameters(myVmArguments);
		generalCommandLine.addParameters(myArguments);
		generalCommandLine.withPassParentEnvironment(myConfiguration.isPassParentEnvs());
		generalCommandLine.withEnvironment(myConfiguration.getEnvs());

		String programParameters = myConfiguration.getProgramParameters();
		if(!StringUtil.isEmpty(programParameters))
		{
			generalCommandLine.addParameters(StringUtil.splitHonorQuotes(programParameters, ' '));
		}

		OSProcessHandler processHandler = new ColoredProcessHandler(generalCommandLine);
		for(ProcessListener processListener : myProcessListeners)
		{
			processHandler.addProcessListener(processListener);
		}
		ConsoleView console = createConsole(processHandler);
		console.attachToProcess(processHandler);
		return new DefaultExecutionResult(console, processHandler);
	}

	@Nonnull
	public ConsoleView createConsole(OSProcessHandler processHandler)
	{
		TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(myConfiguration.getProject());
		return builder.getConsole();
	}
}
