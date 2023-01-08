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

import consulo.configurable.ConfigurationException;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaConfigurationEditor extends SettingsEditor<MochaConfiguration>
{
	private final Project myProject;
	private MochaConfigurationPanel myConfigurationPanel;

	public MochaConfigurationEditor(Project project)
	{
		myProject = project;
	}

	@Override
	@RequiredUIAccess
	protected void resetEditorFrom(MochaConfiguration mochaConfiguration)
	{
		myConfigurationPanel.reset(mochaConfiguration);
	}

	@Override
	@RequiredUIAccess
	protected void applyEditorTo(MochaConfiguration mochaConfiguration) throws ConfigurationException
	{
		myConfigurationPanel.applyTo(mochaConfiguration);
	}

	@Nonnull
	@Override
	protected JComponent createEditor()
	{
		if(myConfigurationPanel == null)
		{
			myConfigurationPanel = new MochaConfigurationPanel(myProject);
		}
		return myConfigurationPanel;
	}
}
