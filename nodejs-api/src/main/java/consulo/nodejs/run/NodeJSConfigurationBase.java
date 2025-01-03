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

import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.execution.CommonProgramRunConfigurationParameters;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.process.ExecutionException;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.SkipEmptySerializationFilter;
import consulo.util.xml.serializer.WriteExternalException;
import consulo.util.xml.serializer.XmlSerializer;
import org.jdom.Element;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private Map<String, String> myEnvs = new HashMap<>();
	private boolean myPassParentEnvs = true;

	public NodeJSConfigurationBase(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	@Override
	@Nonnull
	public abstract List<Module> getValidModules();

	@Nullable
	@Override
	public RunProfileState getState(@Nonnull Executor executor, @Nonnull final ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		Module module = getConfigurationModule().getModule();
		if(module == null && !myUseAlternativeBundle)
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

	@Nonnull
	protected abstract NodeJSRunState createRunState(@Nullable Module module,
			@Nonnull Sdk targetSdk,
			@Nonnull Executor executor,
			@Nonnull ExecutionEnvironment executionEnvironment) throws ExecutionException;

	@Override
	public void writeExternal(Element element) throws WriteExternalException
	{
		super.writeExternal(element);
		XmlSerializer.serializeInto(this, element, new SkipEmptySerializationFilter());
	}

	@Override
	public void readExternal(Element element) throws InvalidDataException
	{
		super.readExternal(element);
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
	public void setEnvs(@Nonnull Map<String, String> envs)
	{
		myEnvs = envs;
	}

	@Nonnull
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
