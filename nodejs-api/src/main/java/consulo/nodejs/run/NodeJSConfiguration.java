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

import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.annotation.access.RequiredReadAction;
import consulo.content.bundle.Sdk;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.process.ExecutionException;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 18.03.14
 */
public class NodeJSConfiguration extends NodeJSConfigurationBase
{

	public NodeJSConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	@Override
	protected Class<NodeJSConfigurationOptions> getOptionsClass()
	{
		return NodeJSConfigurationOptions.class;
	}

	@Override
	public NodeJSConfigurationOptions getOptions()
	{
		return (NodeJSConfigurationOptions) super.getOptions();
	}

	@Override
	protected void convertLegacyState(LegacyRunConfigurationConverter converter)
	{
		super.convertLegacyState(converter);

		NodeJSConfigurationOptions options = new NodeJSConfigurationOptions();
		convertLegacyCommonState(converter, options);
		NodeJSConfigurationLegacyState old = converter.readXml(NodeJSConfigurationLegacyState.class);
		options.setScriptFilePath(old.scriptFilePath);
		converter.setOptions(options);
	}

	@Override
	public List<RunConfigurationFragment<?>> createFragments()
	{
		List<RunConfigurationFragment<?>> fragments = new ArrayList<>();
		fragments.add(new NodeJSScriptFragment());
		fragments.addAll(createCommonFragments());
		return fragments;
	}

	@Override
	@RequiredReadAction
	public List<Module> getValidModules()
	{
		List<Module> list = new ArrayList<Module>();
		for(Module module : ModuleManager.getInstance(getProject()).getModules())
		{
			if(ModuleUtilCore.getExtension(module, NodeJSModuleExtension.class) != null)
			{
				list.add(module);
			}
		}
		return list;
	}


	@Nonnull
	@Override
	protected NodeJSRunState createRunState(@Nullable Module module, @Nonnull Sdk targetSdk, @Nonnull Executor executor, @Nonnull ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		if(getScriptFile() == null)
		{
			throw new ExecutionException("Script file is not found by path '" + getScriptFilePath() + "'");
		}

		NodeJSRunState state = new NodeJSRunState(module, targetSdk, this);
		state.addArgument(getScriptFilePath());
		return state;
	}

	@Nullable
	public VirtualFile getScriptFile()
	{
		String scriptFilePath = getScriptFilePath();
		if(StringUtil.isEmpty(scriptFilePath))
		{
			return null;
		}
		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(scriptFilePath);
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
		return LocalFileSystem.getInstance().findFileByPath(moduleDirPath + "/" + scriptFilePath);
	}

	public String getScriptFilePath()
	{
		return getOptions().getScriptFilePath();
	}

	public void setScriptFilePath(String scriptFilePath)
	{
		getOptions().setScriptFilePath(scriptFilePath);
	}
}
