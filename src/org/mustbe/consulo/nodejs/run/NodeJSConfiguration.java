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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.nodejs.module.extension.NodeJSModuleExtension;
import com.intellij.execution.CommonProgramRunConfigurationParameters;
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
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.SkipEmptySerializationFilter;
import com.intellij.util.xmlb.XmlSerializer;

/**
 * @author VISTALL
 * @since 18.03.14
 */
public class NodeJSConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> implements CommonProgramRunConfigurationParameters
{
	private String myScriptFilePath;
	private String myVmParameters;
	private String myProgramParameters;
	private String myWorkingDirectory;
	private boolean myUseAlternativeBundle;
	private String myAlternativeBundleName;
	private Map<String, String> myEnvs = new HashMap<String, String>();
	private boolean myPassParentEnvs = true;

	public NodeJSConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
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
		return new NodeJSConfigurationEditor(getProject());
	}

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull final ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		Module module = getConfigurationModule().getModule();
		if(module == null)
		{
			throw new ExecutionException("No module");
		}

		Sdk targetSdk = null;
		if(myUseAlternativeBundle)
		{
			if(StringUtil.isEmpty(myAlternativeBundleName))
			{
				throw new ExecutionException("NodeJS alternative bundle is empty");
			}

			targetSdk = SdkTable.getInstance().findSdk(myAlternativeBundleName);
			if(targetSdk == null)
			{
				throw new ExecutionException("NodeJS alternative bundle '" + myAlternativeBundleName + "' is not found");
			}
		}
		else
		{
			targetSdk = ModuleUtilCore.getSdk(module, NodeJSModuleExtension.class);
			if(targetSdk == null)
			{
				throw new ExecutionException("NodeJS bundle is undefined in module '" + module.getName() + "'");
			}
		}

		return new NodeJSRunState(module, targetSdk, this);
	}

	@Override
	public void writeExternal(Element element) throws WriteExternalException
	{
		super.writeExternal(element);
		writeModule(element);
		XmlSerializer.serializeInto(this, element, new SkipEmptySerializationFilter());
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException
	{
		super.readExternal(element);
		readModule(element);
		XmlSerializer.deserializeInto(this, element);
	}

	@Nullable
	public VirtualFile getScriptFile()
	{
		if(StringUtil.isEmpty(myScriptFilePath))
		{
			return null;
		}
		VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(myScriptFilePath);
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
		return LocalFileSystem.getInstance().findFileByPath(moduleDirPath + "/" + myScriptFilePath);
	}

	public String getScriptFilePath()
	{
		return myScriptFilePath;
	}

	public void setScriptFilePath(String scriptFilePath)
	{
		myScriptFilePath = scriptFilePath;
	}

	public String getVmParameters()
	{
		return myVmParameters;
	}

	public void setVmParameters(String vmParameters)
	{
		myVmParameters = vmParameters;
	}

	@Override
	public void setProgramParameters(@Nullable String value)
	{
		myProgramParameters = value;
	}

	@Nullable
	@Override
	public String getProgramParameters()
	{
		return myProgramParameters;
	}

	@Override
	public void setWorkingDirectory(@Nullable String value)
	{
		myWorkingDirectory = value;
	}

	@Nullable
	@Override
	public String getWorkingDirectory()
	{
		return myWorkingDirectory;
	}

	@Override
	public void setEnvs(@NotNull Map<String, String> envs)
	{
		myEnvs = envs;
	}

	@NotNull
	@Override
	public Map<String, String> getEnvs()
	{
		return myEnvs;
	}

	@Override
	public void setPassParentEnvs(boolean passParentEnvs)
	{
		myPassParentEnvs = passParentEnvs;
	}

	@Override
	public boolean isPassParentEnvs()
	{
		return myPassParentEnvs;
	}

	public boolean isUseAlternativeBundle()
	{
		return myUseAlternativeBundle;
	}

	public void setUseAlternativeBundle(boolean useAlternativeBundle)
	{
		myUseAlternativeBundle = useAlternativeBundle;
	}

	public String getAlternativeBundleName()
	{
		return myAlternativeBundleName;
	}

	public void setAlternativeBundleName(String alternativeBundleName)
	{
		myAlternativeBundleName = alternativeBundleName;
	}
}
