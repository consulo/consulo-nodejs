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

import consulo.disposer.Disposable;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.mocha.localize.MochaLocalize;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.nodejs.run.NodeJSConfigurationPanelBase;
import consulo.project.Project;
import consulo.ui.Label;
import consulo.ui.RadioGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.HorizontalLayout;
import consulo.ui.util.FormBuilder;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaConfigurationPanel extends NodeJSConfigurationPanelBase<MochaConfiguration>
{
	private final RadioGroup<MochaConfiguration.TargetType> myTargetGroup;
	private final HorizontalLayout myTargetPanel;

	private final Label myDirectoryLabel;
	private final FileChooserTextBoxBuilder.Controller myDirectoryField;
	private final Label myFileLabel;
	private final FileChooserTextBoxBuilder.Controller myFileField;

	@RequiredUIAccess
	public MochaConfigurationPanel(Project project, Disposable uiDisposable)
	{
		super(project, uiDisposable);

		myTargetGroup = RadioGroup.create();
		myTargetPanel = HorizontalLayout.create();
		myTargetPanel.add(myTargetGroup.newButton(MochaLocalize.runConfigurationDirectory(), MochaConfiguration.TargetType.DIRECTORY));
		myTargetPanel.add(myTargetGroup.newButton(MochaLocalize.runConfigurationFile(), MochaConfiguration.TargetType.FILE));
		myTargetGroup.addValueListener(targetType -> updateTargetFields());

		myDirectoryLabel = Label.create(LocalizeValue.join(MochaLocalize.runConfigurationDirectory(), LocalizeValue.colon()));
		myDirectoryField = createModuleRelativePathField(NodeJSLocalize.runConfigurationSelectScriptTitle(),
				MochaLocalize.runConfigurationSelectScriptDirectory(),
				new FileChooserDescriptor(false, true, false, false, false, false));

		myFileLabel = Label.create(LocalizeValue.join(MochaLocalize.runConfigurationFile(), LocalizeValue.colon()));
		myFileField = createModuleRelativePathField(NodeJSLocalize.runConfigurationSelectScriptTitle(),
				MochaLocalize.runConfigurationSelectScriptFile(),
				new FileChooserDescriptor(true, false, false, false, false, false));
	}

	@Override
	@RequiredUIAccess
	protected void addBefore(FormBuilder builder)
	{
		addVmParameters(builder);
	}

	@Override
	@RequiredUIAccess
	protected void addAfter(FormBuilder builder)
	{
		addModuleAndBundle(builder);

		builder.addLabeled(MochaLocalize.runConfigurationTestInLabel(), myTargetPanel);
		builder.addLabeled(myDirectoryLabel, myDirectoryField.getComponent());
		builder.addLabeled(myFileLabel, myFileField.getComponent());

		updateTargetFields();
	}

	@RequiredUIAccess
	private MochaConfiguration.TargetType getTargetType()
	{
		return myTargetGroup.getValue() == MochaConfiguration.TargetType.FILE ? MochaConfiguration.TargetType.FILE : MochaConfiguration.TargetType.DIRECTORY;
	}

	@RequiredUIAccess
	private void updateTargetFields()
	{
		boolean directory = getTargetType() == MochaConfiguration.TargetType.DIRECTORY;

		myDirectoryLabel.setVisible(directory);
		myDirectoryField.getComponent().setVisible(directory);
		myFileLabel.setVisible(!directory);
		myFileField.getComponent().setVisible(!directory);
	}

	@Override
	@RequiredUIAccess
	public void reset(MochaConfiguration configuration)
	{
		super.reset(configuration);

		myTargetGroup.setValue(configuration.getTargetType(), false);
		resetPathField(myDirectoryField, configuration.getDirectoryPath());
		resetPathField(myFileField, configuration.getFilePath());

		updateTargetFields();
	}

	@Override
	@RequiredUIAccess
	public void apply(MochaConfiguration configuration)
	{
		super.apply(configuration);

		configuration.setTargetType(getTargetType());
		configuration.setDirectoryPath(myDirectoryField.getValue());
		configuration.setFilePath(myFileField.getValue());
	}
}
