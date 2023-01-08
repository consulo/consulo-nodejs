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

import consulo.content.bundle.SdkTypeId;
import consulo.execution.CommonProgramRunConfigurationParameters;
import consulo.execution.ui.awt.CommonProgramParametersPanel;
import consulo.execution.ui.awt.RawCommandLineEditor;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.ide.setting.bundle.SettingsSdksModel;
import consulo.module.Module;
import consulo.module.ui.awt.ModuleListCellRenderer;
import consulo.module.ui.awt.SdkComboBox;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.CollectionComboBoxModel;
import consulo.ui.ex.awt.ComboBox;
import consulo.ui.ex.awt.LabeledComponent;
import consulo.util.lang.StringUtil;
import consulo.util.lang.function.Conditions;

import javax.swing.*;
import java.awt.*;

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
		SettingsSdksModel sdksModel = ShowSettingsUtil.getInstance().getSdksModel();

		myAlternativeBundleComboBox = new SdkComboBox(sdksModel, Conditions.<SdkTypeId>is(NodeJSBundleType.getInstance()), true);
		myAlternativeBundleComboBox.setEnabled(false);
		myUseAlternativeBundleCheckBox.addItemListener(e -> myAlternativeBundleComboBox.setEnabled(myUseAlternativeBundleCheckBox.isSelected()));
		super.initComponents();

		setPreferredSize(null);
	}

	protected void addComponentsInternal()
	{
		super.addComponents();
	}

	@Override
	protected abstract void addComponents();

	@Override
	@RequiredUIAccess
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
	@RequiredUIAccess
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
