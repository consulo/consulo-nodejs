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

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.project.Project;
import consulo.mocha.MochaIcons;
import consulo.mocha.module.extension.MochaModuleExtension;
import consulo.module.extension.ModuleExtensionHelper;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaConfigurationType extends ConfigurationTypeBase
{
	@NotNull
	public static MochaConfigurationType getInstance()
	{
		return CONFIGURATION_TYPE_EP.findExtension(MochaConfigurationType.class);
	}

	public MochaConfigurationType()
	{
		super("#MochaConfigurationType", "Mocha", "", MochaIcons.Mocha);

		addFactory(new ConfigurationFactory(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new MochaConfiguration("Unnamed", new RunConfigurationModule(project), this);
			}

			@Override
			public boolean isApplicable(@NotNull Project project)
			{
				return ModuleExtensionHelper.getInstance(project).hasModuleExtension(MochaModuleExtension.class);
			}
		});
	}
}
