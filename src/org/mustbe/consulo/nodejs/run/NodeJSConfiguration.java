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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.nodejs.module.extension.NodeJSModuleExtension;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.val;

/**
 * @author VISTALL
 * @since 18.03.14
 */
public class NodeJSConfiguration extends ModuleBasedConfiguration<RunConfigurationModule>
{
	private String myScriptName;

	public NodeJSConfiguration(RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(configurationModule, factory);
	}

	public NodeJSConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	@Override
	public Collection<Module> getValidModules()
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

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
	{
		return new NodeJSConfigurationEditor(getProject(), this);
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull final ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		val module = getConfigurationModule().getModule();
		if(module == null)
		{
			throw new ExecutionException("No module");
		}
		final Sdk sdk = ModuleUtilCore.getSdk(module, NodeJSModuleExtension.class);
		if(sdk == null)
		{
			throw new ExecutionException("NodeJS bundle is not set");
		}

		return new NodeJSRunState(module, myScriptName, sdk);
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException
	{
		super.writeExternal(element);
		writeModule(element);
		element.setAttribute("script-file", StringUtil.notNullize(myScriptName));
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException
	{
		super.readExternal(element);
		readModule(element);
		myScriptName =  element.getAttributeValue("script-file");
	}

	@Nullable
	public VirtualFile getScriptFile()
	{
		if(StringUtil.isEmpty(myScriptName))
		{
			return null;
		}
		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(myScriptName);
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
		return LocalFileSystem.getInstance().findFileByPath(moduleDirPath + "/" + myScriptName);
	}

	public String getScriptName()
	{
		return myScriptName;
	}

	public void setScriptName(String scriptName)
	{
		myScriptName = scriptName;
	}
}
