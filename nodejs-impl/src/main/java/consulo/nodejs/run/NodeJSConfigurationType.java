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

import javax.annotation.Nonnull;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.project.Project;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.nodejs.NodeJSIcons;
import consulo.nodejs.module.extension.NodeJSModuleExtension;

/**
 * @author VISTALL
 * @since 18.03.14
 */
public class NodeJSConfigurationType extends ConfigurationTypeBase
{
	@Nonnull
	public static NodeJSConfigurationType getInstance()
	{
		return CONFIGURATION_TYPE_EP.findExtensionOrFail(NodeJSConfigurationType.class);
	}

	public NodeJSConfigurationType()
	{
		super("NodeJSConfigurationType", "NodeJS", "", NodeJSIcons.NodeJS);

		addFactory(new ConfigurationFactory(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new NodeJSConfiguration("Unnamed", new RunConfigurationModule(project), this);
			}

			@Override
			public boolean isApplicable(@Nonnull Project project)
			{
				return ModuleExtensionHelper.getInstance(project).hasModuleExtension(NodeJSModuleExtension.class);
			}
		});
	}
}
