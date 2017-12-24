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

import javax.swing.JTextField;

import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.JBUI;
import consulo.annotations.RequiredDispatchThread;

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
		add(LabeledComponent.left(myScriptTextField, "Script"));
		add(myVmParametersComponent);
		addComponentsInternal();
		add(LabeledComponent.left(myModuleBox, "Module"));
		add(JBUI.Panels.simplePanel().addToLeft(myUseAlternativeBundleCheckBox).addToCenter(myAlternativeBundleComboBox));
	}

	@Override
	@RequiredDispatchThread
	public void applyTo(CommonProgramRunConfigurationParameters configuration)
	{
		super.applyTo(configuration);
		NodeJSConfiguration nodeJSConfiguration = (NodeJSConfiguration) configuration;

		nodeJSConfiguration.setScriptFilePath(myScriptTextField.getText());
	}

	@Override
	@RequiredDispatchThread
	public void reset(CommonProgramRunConfigurationParameters configuration)
	{
		super.reset(configuration);
		NodeJSConfiguration nodeJSConfiguration = (NodeJSConfiguration) configuration;

		myScriptTextField.setText(nodeJSConfiguration.getScriptFilePath());
	}
}
