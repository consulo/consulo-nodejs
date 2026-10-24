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
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.ModuleBasedConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.fragment.EnvironmentFragmentState;
import consulo.execution.configuration.fragment.ProgramParametersFragmentState;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.WorkingDirectoryFragmentState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.process.ExecutionException;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public abstract class NodeJSConfigurationBase extends ModuleBasedConfiguration<RunConfigurationModule> implements CommonProgramRunConfigurationParameters
{

	public NodeJSConfigurationBase(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	@Override
	public NodeJSConfigurationBaseOptions getOptions()
	{
		return (NodeJSConfigurationBaseOptions) super.getOptions();
	}

	/**
	 * Converts the settings an old configuration element wrote with the bean serializer
	 */
	protected static void convertLegacyCommonState(LegacyRunConfigurationConverter converter, NodeJSConfigurationBaseOptions options)
	{
		NodeJSConfigurationBaseLegacyState old = converter.readXml(NodeJSConfigurationBaseLegacyState.class);
		options.setVmParameters(old.vmParameters);
		options.setUseAlternativeBundle(old.useAlternativeBundle);
		options.setAlternativeBundleName(old.alternativeBundleName);

		converter.addFragment(new ProgramParametersFragmentState(old.programParameters));
		converter.addFragment(new WorkingDirectoryFragmentState(old.workingDirectory));
		converter.addFragment(new EnvironmentFragmentState(old.envs, old.passParentEnvs));
	}

	/**
	 * Fragments shared by the Node.js based configurations, after their own target ones
	 */
	protected List<RunConfigurationFragment<?>> createCommonFragments()
	{
		return List.of(new NodeJSVmParametersFragment<>(), new NodeJSAlternativeBundleFragment<>());
	}

	@Override
	@Nonnull
	public abstract List<Module> getValidModules();

	@Nullable
	@Override
	public RunProfileState getState(@Nonnull Executor executor, @Nonnull final ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		Module module = getConfigurationModule().getModule();
		if(module == null && !isUseAlternativeBundle())
		{
			throw new ExecutionException("Module is not set for run configuration");
		}

		Sdk targetSdk;
		String alternativeBundleName = getAlternativeBundleName();
		if(isUseAlternativeBundle())
		{
			if(StringUtil.isEmpty(alternativeBundleName))
			{
				throw new ExecutionException("NodeJS alternative bundle is empty");
			}

			targetSdk = SdkTable.getInstance().findSdk(alternativeBundleName);
			if(targetSdk == null)
			{
				throw new ExecutionException("NodeJS alternative bundle '" + alternativeBundleName + "' is not found");
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



	@Nullable
	public String getVmParameters()
	{
		return getOptions().getVmParameters();
	}

	public void setVmParameters(@Nullable String vmParameters)
	{
		getOptions().setVmParameters(vmParameters);
	}

	public boolean isUseAlternativeBundle()
	{
		return getOptions().isUseAlternativeBundle();
	}

	public void setUseAlternativeBundle(boolean useAlternativeBundle)
	{
		getOptions().setUseAlternativeBundle(useAlternativeBundle);
	}

	@Nullable
	public String getAlternativeBundleName()
	{
		return getOptions().getAlternativeBundleName();
	}

	public void setAlternativeBundleName(@Nullable String alternativeBundleName)
	{
		getOptions().setAlternativeBundleName(alternativeBundleName);
	}
}
