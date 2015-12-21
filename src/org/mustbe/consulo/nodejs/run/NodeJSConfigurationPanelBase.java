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

import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.nodejs.bundle.NodeJSBundleType;
import com.intellij.application.options.ModuleListCellRenderer;
import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.execution.ui.CommonProgramParametersPanel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.roots.ui.configuration.SdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.RawCommandLineEditor;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public abstract class NodeJSConfigurationPanelBase extends CommonProgramParametersPanel
{
	protected LabeledComponent<RawCommandLineEditor> myVmParametersComponent;
	protected ComboBox myModuleBox;
	protected JCheckBox myUseAlternativeBundleCheckBox;
	protected SdkComboBox myAlternativeBundleComboBox;

	protected final Project myProject;

	public NodeJSConfigurationPanelBase(Project project)
	{
		myProject = project;
	}

	@Override
	protected void initComponents()
	{
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

	protected void addComponentsInternal()
	{
		super.addComponents();
	}

	@Override
	protected abstract void addComponents();

	@Override
	@RequiredDispatchThread
	public void applyTo(CommonProgramRunConfigurationParameters configuration)
	{
		super.applyTo(configuration);
		NodeJSConfigurationBase nodeJSConfiguration = (NodeJSConfigurationBase) configuration;

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
		NodeJSConfigurationBase nodeJSConfiguration = (NodeJSConfigurationBase) configuration;

		myVmParametersComponent.getComponent().setText(nodeJSConfiguration.getVmParameters());
		myModuleBox.setModel(new CollectionComboBoxModel(nodeJSConfiguration.getValidModules()));
		myModuleBox.setSelectedItem(nodeJSConfiguration.getConfigurationModule().getModule());
		myUseAlternativeBundleCheckBox.setSelected(nodeJSConfiguration.isUseAlternativeBundle());
		myAlternativeBundleComboBox.setSelectedSdk(nodeJSConfiguration.getAlternativeBundleName());
	}
}
