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

import org.mustbe.consulo.nodejs.run.NodeJSConfigurationPanelBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.util.ui.JBUI;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaConfigurationPanel extends NodeJSConfigurationPanelBase
{
	public MochaConfigurationPanel(Project project)
	{
		super(project);
	}

	@Override
	protected void addComponents()
	{
		add(myVmParametersComponent);
		addComponentsInternal();
		add(LabeledComponent.left(myModuleBox, "Module"));
		add(JBUI.Panels.simplePanel().addToLeft(myUseAlternativeBundleCheckBox).addToCenter(myAlternativeBundleComboBox));

		getProgramParametersComponent().setVisible(false);
	}
}
