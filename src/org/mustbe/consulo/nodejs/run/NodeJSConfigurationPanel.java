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

package org.mustbe.consulo.nodejs.run;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.nodejs.bundle.NodeJSBundleType;
import com.intellij.application.options.ModuleListCellRenderer;
import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.SdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.util.ui.JBUI;

/**
 * @author VISTALL
 * @since 04.12.2015
 */
public class NodeJSConfigurationPanel extends CommonProgramParametersPanel
{
	private LabeledComponent<RawCommandLineEditor> myVmParametersComponent;
	private TextFieldWithBrowseButton myScriptTextField;
	private ComboBox myModuleBox;
	private JCheckBox myUseAlternativeBundleCheckBox;
	private SdkComboBox myAlternativeBundleComboBox;

	private final Project myProject;

	public NodeJSConfigurationPanel(Project project)
	{
		myProject = project;
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

		myModuleBox = new ComboBox();
		myModuleBox.setRenderer(new ModuleListCellRenderer());

		myVmParametersComponent = LabeledComponent.create(new RawCommandLineEditor(), "VM arguments");
		myVmParametersComponent.setLabelLocation(BorderLayout.WEST);
		copyDialogCaption(myVmParametersComponent);

		myUseAlternativeBundleCheckBox = new JCheckBox("Use alternative bundle: ");
		ProjectSdksModel projectSdksModel = new ProjectSdksModel();
		projectSdksModel.reset();

		myAlternativeBundleComboBox = new SdkComboBox(projectSdksModel, Conditions.<SdkTypeId>is(NodeJSBundleType.getInstance()), true);
		myAlternativeBundleComboBox.setEnabled(false);
		myUseAlternativeBundleCheckBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				myAlternativeBundleComboBox.setEnabled(myUseAlternativeBundleCheckBox.isSelected());
			}
		});
		super.initComponents();
	}

	@Override
	protected void addComponents()
	{
		add(LabeledComponent.left(myScriptTextField, "Script"));
		add(myVmParametersComponent);
		super.addComponents();
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
		nodeJSConfiguration.setVmParameters(myVmParametersComponent.getComponent().getText());
		nodeJSConfiguration.getConfigurationModule().setModule((Module) myModuleBox.getSelectedItem());
		nodeJSConfiguration.setUseAlternativeBundle(myUseAlternativeBundleCheckBox.isSelected());
		nodeJSConfiguration.setAlternativeBundleName(StringUtil.nullize(myAlternativeBundleComboBox.getSelectedSdkName()));
	}

	@Override
	@RequiredDispatchThread
	public void reset(CommonProgramRunConfigurationParameters configuration)
	{
		super.reset(configuration);
		NodeJSConfiguration nodeJSConfiguration = (NodeJSConfiguration) configuration;

		myVmParametersComponent.getComponent().setText(((NodeJSConfiguration) configuration).getVmParameters());
		myModuleBox.setModel(new CollectionComboBoxModel(nodeJSConfiguration.getValidModules()));
		myModuleBox.setSelectedItem(nodeJSConfiguration.getConfigurationModule().getModule());
		myScriptTextField.setText(nodeJSConfiguration.getScriptFilePath());
		myUseAlternativeBundleCheckBox.setSelected(nodeJSConfiguration.isUseAlternativeBundle());
		myAlternativeBundleComboBox.setSelectedSdk(nodeJSConfiguration.getAlternativeBundleName());
	}
}
