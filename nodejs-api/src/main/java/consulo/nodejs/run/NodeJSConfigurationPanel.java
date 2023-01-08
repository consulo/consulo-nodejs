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

import consulo.annotation.access.RequiredReadAction;
import consulo.execution.CommonProgramRunConfigurationParameters;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.ex.awt.JBUI;
import consulo.ui.ex.awt.LabeledComponent;
import consulo.ui.ex.awt.TextComponentAccessor;
import consulo.ui.ex.awt.TextFieldWithBrowseButton;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 04.12.2015
 */
public class NodeJSConfigurationPanel extends NodeJSConfigurationPanelBase
{
	private TextFieldWithBrowseButton myScriptTextField;

	public NodeJSConfigurationPanel(Project project)
	{
		super(project);
	}

	@Override
	protected void initComponents()
	{
		myScriptTextField = new TextFieldWithBrowseButton();
		myScriptTextField.addBrowseFolderListener("Select Script", "Select Script File For Execution", myProject, new FileChooserDescriptor(true, false, false, false, false, false),
				new TextComponentAccessor<JTextField>()
		{
			@Override
			public String getText(JTextField textField)
			{
				return textField.getText();
			}

			@Override
			public void setText(JTextField textField, String text)
			{
				Module selectedItem = (Module) myModuleBox.getSelectedItem();
				if(selectedItem == null)
				{
					textField.setText(text);
				}
				else
				{
					String moduleDirPath = selectedItem.getModuleDirPath();
					String relativePath = moduleDirPath == null ? null : FileUtil.getRelativePath(moduleDirPath, FileUtil.toSystemIndependentName(text), '/');
					if(StringUtil.isEmpty(relativePath))
					{
						relativePath = text;
					}
					textField.setText(relativePath);
				}
			}
		});

		super.initComponents();
	}

	@Override
	protected void addComponents()
	{
		add(LabeledComponent.create(myScriptTextField, "Script"));
		add(myVmParametersComponent);
		addComponentsInternal();
		add(LabeledComponent.create(myModuleBox, "Module"));
		add(JBUI.Panels.simplePanel().addToLeft(myUseAlternativeBundleCheckBox).addToCenter(myAlternativeBundleComboBox));
	}

	@Override
	@RequiredReadAction
	public void applyTo(CommonProgramRunConfigurationParameters configuration)
	{
		super.applyTo(configuration);
		NodeJSConfiguration nodeJSConfiguration = (NodeJSConfiguration) configuration;

		nodeJSConfiguration.setScriptFilePath(myScriptTextField.getText());
	}

	@Override
	@RequiredReadAction
	public void reset(CommonProgramRunConfigurationParameters configuration)
	{
		super.reset(configuration);
		NodeJSConfiguration nodeJSConfiguration = (NodeJSConfiguration) configuration;

		myScriptTextField.setText(nodeJSConfiguration.getScriptFilePath());
	}
}
