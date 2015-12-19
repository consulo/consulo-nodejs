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

import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredDispatchThread;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;

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
	@RequiredDispatchThread
	protected void resetEditorFrom(MochaConfiguration mochaConfiguration)
	{
		myConfigurationPanel.reset(mochaConfiguration);
	}

	@Override
	@RequiredDispatchThread
	protected void applyEditorTo(MochaConfiguration mochaConfiguration) throws ConfigurationException
	{
		myConfigurationPanel.applyTo(mochaConfiguration);
	}

	@NotNull
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