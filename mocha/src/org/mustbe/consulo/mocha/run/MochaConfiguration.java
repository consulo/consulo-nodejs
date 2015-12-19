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

package org.mustbe.consulo.mocha.run;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.mocha.module.extension.MochaModuleExtension;
import org.mustbe.consulo.nodejs.run.NodeJSConfigurationBase;
import org.mustbe.consulo.nodejs.run.NodeJSRunState;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.projectRoots.Sdk;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaConfiguration extends NodeJSConfigurationBase
{
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

	@NotNull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
	{
		return new MochaConfigurationEditor(getProject());
	}

	@NotNull
	@Override
	protected NodeJSRunState createRunState(@NotNull Module module, @NotNull Sdk targetSdk)
	{
		return new NodeJSRunState(module, targetSdk, this);
	}
}
