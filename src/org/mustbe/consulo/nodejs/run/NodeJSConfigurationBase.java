/*
 * Copyright 2013-2015 must-be.org
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.nodejs.module.extension.NodeJSModuleExtension;
import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.SkipEmptySerializationFilter;
import com.intellij.util.xmlb.XmlSerializer;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public abstract class NodeJSConfigurationBase extends ModuleBasedConfiguration<RunConfigurationModule> implements CommonProgramRunConfigurationParameters
{
	private String myVmParameters;
	private String myWorkingDirectory;
	private boolean myUseAlternativeBundle;
	private String myAlternativeBundleName;
	private String myProgramParameters;
	private Map<String, String> myEnvs = new HashMap<String, String>();
	private boolean myPassParentEnvs = true;

	public NodeJSConfigurationBase(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	@Override
	public abstract List<Module> getValidModules();

	@Nullable
	@Override
	public RunProfileState getState(@NotNull Executor executor, @NotNull final ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		Module module = getConfigurationModule().getModule();
		if(module == null)
		{
			throw new ExecutionException("Module is not set for run configuration");
		}

		Sdk targetSdk;
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

		return createRunState(module, targetSdk, executor, executionEnvironment);
	}

	@NotNull
	protected abstract NodeJSRunState createRunState(@NotNull Module module,
			@NotNull Sdk targetSdk,
			@NotNull Executor executor,
			@NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException;

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
