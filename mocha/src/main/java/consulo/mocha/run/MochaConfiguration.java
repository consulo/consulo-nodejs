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

package consulo.mocha.run;

import consulo.annotation.access.RequiredReadAction;
import consulo.container.plugin.PluginManager;
import consulo.content.bundle.Sdk;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.test.TestConsoleProperties;
import consulo.execution.test.sm.SMTestRunnerConnectionUtil;
import consulo.execution.test.sm.runner.SMTRunnerConsoleProperties;
import consulo.execution.ui.console.ConsoleView;
import consulo.language.util.ModuleUtilCore;
import consulo.mocha.module.extension.MochaModuleExtension;
import consulo.mocha.psi.MochaPsiElementUtil;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nodejs.packages.call.NpmRunUtil;
import consulo.nodejs.run.NodeJSConfigurationBase;
import consulo.nodejs.run.NodeJSRunState;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaConfiguration extends NodeJSConfigurationBase
{
	public static enum TargetType
	{
		DIRECTORY,
		FILE
	}

	private TargetType myTargetType = TargetType.DIRECTORY;
	private String myFilePath;
	private String myDirectoryPath;

	public MochaConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	@Override
	@RequiredReadAction
	public List<Module> getValidModules()
	{
		List<Module> list = new ArrayList<Module>();
		for(Module module : ModuleManager.getInstance(getProject()).getModules())
		{
			if(ModuleUtilCore.getExtension(module, MochaModuleExtension.class) != null)
			{
				list.add(module);
			}
		}
		return list;
	}

	@Nonnull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
	{
		return new MochaConfigurationEditor(getProject());
	}

	@Nonnull
	@Override
	protected NodeJSRunState createRunState(@Nullable Module module,
			@Nonnull Sdk targetSdk,
			@Nonnull final Executor executor,
			@Nonnull final ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		VirtualFile mocha = module == null ? null : NpmRunUtil.findNpmModule(module, MochaPsiElementUtil.MOCHA);
		if(mocha == null)
		{
			throw new ExecutionException("'mocha' module is not installed");
		}

		VirtualFile fileOrDirectory = getFileOrDirectory();
		if(fileOrDirectory == null)
		{
			throw new ExecutionException((myTargetType == TargetType.DIRECTORY ? "Directory" : "File") + " is not set");
		}

		NodeJSRunState state = new NodeJSRunState(module, targetSdk, this)
		{
			@Nonnull
			@Override
			public ConsoleView createConsole(ProcessHandler processHandler)
			{
				SMTRunnerConsoleProperties testConsoleProperties = new SMTRunnerConsoleProperties(MochaConfiguration.this, "Mocha", executor);
				testConsoleProperties.setIdBasedTestTree(true);

				testConsoleProperties.setIfUndefined(TestConsoleProperties.HIDE_PASSED_TESTS, false);

				return SMTestRunnerConnectionUtil.createConsole("Mocha", testConsoleProperties);
			}
		};
		state.addArgument(mocha.getPath() + "/bin/_mocha");

		File pluginPath = PluginManager.getPluginPath(MochaConfiguration.class);

		File mochaReporter = new File(pluginPath, "mocha-consulo");
		if(mochaReporter.exists())
		{
			state.addArgument("--reporter");
			state.addArgument(new File(mochaReporter, "lib/mochaIntellijReporter.js").getPath());

			state.addArgument("--ui");
			state.addArgument("bdd");
		}

		switch(myTargetType)
		{
			case DIRECTORY:
				state.addArgument("--recursive");
				state.addArgument(getDirectoryPath());
				break;
			case FILE:
				state.addArgument(getFilePath());
				break;
		}
		return state;
	}

	@Nullable
	public String getPath()
	{
		return myTargetType == TargetType.FILE ? myFilePath : myDirectoryPath;
	}

	@Nullable
	public VirtualFile getFileOrDirectory()
	{
		String path = getPath();

		if(StringUtil.isEmpty(path))
		{
			return null;
		}
		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(path);
		if(fileByPath != null)
		{
			return fileByPath;
		}
		Module module = getConfigurationModule().getModule();
		if(module == null)
		{
			return null;
		}
		String moduleDirPath = module.getModuleDirPath();
		if(moduleDirPath == null)
		{
			return null;
		}
		return LocalFileSystem.getInstance().findFileByPath(moduleDirPath + "/" + path);
	}

	@Nonnull
	public TargetType getTargetType()
	{
		return myTargetType;
	}

	public void setTargetType(TargetType targetType)
	{
		myTargetType = targetType;
	}

	public String getDirectoryPath()
	{
		return myDirectoryPath;
	}

	public void setDirectoryPath(String directoryPath)
	{
		myDirectoryPath = directoryPath;
	}

	public String getFilePath()
	{
		return myFilePath;
	}

	public void setFilePath(String filePath)
	{
		myFilePath = filePath;
	}
}
