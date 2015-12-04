/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.nodejs.run;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jetbrains.annotations.NotNull;
import com.intellij.application.options.ModuleListCellRenderer;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author VISTALL
 * @since 18.03.14
 */
public class NodeJSConfigurationEditor extends SettingsEditor<NodeJSConfiguration>
{
	private final NodeJSConfiguration myNodeJSConfiguration;
	private JPanel myRoot;
	private TextFieldWithBrowseButton myScriptTextField;
	private ComboBox myModuleBox;

	public NodeJSConfigurationEditor(Project project, NodeJSConfiguration nodeJSConfiguration)
	{
		myNodeJSConfiguration = nodeJSConfiguration;

		myScriptTextField.addBrowseFolderListener("Select Script", "Select Script File For Execution", project, new FileChooserDescriptor(true,
				false, false, false, false, false), new TextComponentAccessor<JTextField>()
		{
			@Override
			public String getText(JTextField jTextField)
			{
				return jTextField.getText();
			}

			@Override
			public void setText(JTextField jTextField, String text)
			{
				Module selectedItem = (Module) myModuleBox.getSelectedItem();
				if(selectedItem == null)
				{
					jTextField.setText(text);
				}
				else
				{
					String moduleDirPath = selectedItem.getModuleDirPath();
					String relativePath = moduleDirPath == null ? null : FileUtil.getRelativePath(moduleDirPath, FileUtil.toSystemIndependentName(text), '/');
					if(StringUtil.isEmpty(relativePath))
					{
						relativePath = text;
					}
					jTextField.setText(relativePath);
				}
			}
		});
	}

	@Override
	protected void resetEditorFrom(NodeJSConfiguration nodeJSConfiguration)
	{
		myScriptTextField.setText(nodeJSConfiguration.getScriptFilePath());
		myModuleBox.setSelectedItem(nodeJSConfiguration.getConfigurationModule().getModule());
	}

	@Override
	protected void applyEditorTo(NodeJSConfiguration nodeJSConfiguration) throws ConfigurationException
	{
		nodeJSConfiguration.setScriptFilePath(myScriptTextField.getText());
		nodeJSConfiguration.getConfigurationModule().setModule((Module) myModuleBox.getSelectedItem());
	}

	@NotNull
	@Override
	protected JComponent createEditor()
	{
		return myRoot;
	}

	private void createUIComponents()
	{
		myModuleBox = new ComboBox(myNodeJSConfiguration.getValidModules().toArray());
		myModuleBox.setRenderer(new ModuleListCellRenderer());
	}
}
