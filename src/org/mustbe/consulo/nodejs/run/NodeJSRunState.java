package org.mustbe.consulo.nodejs.run;

import java.util.List;

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

	public NodeJSRunState(Module module, String scriptName, Sdk sdk)
	{
		myModule = module;
		myScriptName = scriptName;
		mySdk = sdk;
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
		generalCommandLine.setExePath(NodeJSBundleType.getExePath(mySdk));
		generalCommandLine.addParameters(myAdditionalArguments);
		generalCommandLine.addParameter(myScriptName);

		TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(myModule.getProject());
		ConsoleView console = builder.getConsole();
		OSProcessHandler processHandler = new OSProcessHandler(generalCommandLine);
		console.attachToProcess(processHandler);
		return new DefaultExecutionResult(console, processHandler);
	}
}
