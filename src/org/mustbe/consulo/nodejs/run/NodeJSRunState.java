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

import java.util.List;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.nodejs.bundle.NodeJSBundleType;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class NodeJSRunState implements RunProfileState
{
	private final Module myModule;
	private final String myScriptName;
	private final Sdk mySdk;
	private final List<String> myAdditionalArguments = new SmartList<String>();
	private final List<ProcessListener> myProcessListeners = new SmartList<ProcessListener>();

	public NodeJSRunState(Module module, String scriptName, Sdk sdk)
	{
		myModule = module;
		myScriptName = scriptName;
		mySdk = sdk;
	}

	public void addProcessListener(ProcessListener processListener)
	{
		myProcessListeners.add(processListener);
	}

	public void addArgument(String argument)
	{
		myAdditionalArguments.add(argument);
	}

	@Nullable
	@Override
	public ExecutionResult execute(Executor executor, @NotNull ProgramRunner programRunner) throws ExecutionException
	{
		GeneralCommandLine generalCommandLine = new GeneralCommandLine();
		generalCommandLine.setWorkDirectory(myModule.getModuleDirPath());
		generalCommandLine.setExePath(NodeJSBundleType.getExePath(mySdk).getPath());
		generalCommandLine.addParameters(myAdditionalArguments);
		generalCommandLine.addParameter(myScriptName);

		TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(myModule.getProject());
		ConsoleView console = builder.getConsole();
		OSProcessHandler processHandler = new OSProcessHandler(generalCommandLine);
		for(ProcessListener processListener : myProcessListeners)
		{
			processHandler.addProcessListener(processListener);
		}
		console.attachToProcess(processHandler);
		return new DefaultExecutionResult(console, processHandler);
	}
}
