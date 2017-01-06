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

import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.jdesktop.swingx.HorizontalLayout;
import consulo.nodejs.run.NodeJSConfigurationPanelBase;
import com.intellij.execution.CommonProgramRunConfigurationParameters;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import consulo.annotations.RequiredDispatchThread;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaConfigurationPanel extends NodeJSConfigurationPanelBase
{
	private JRadioButton myDirectoryButton;
	private JRadioButton myFileButton;

	private TextFieldWithBrowseButton myDirectoryField;
	private TextFieldWithBrowseButton myFileField;

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

		ButtonGroup targetGroup = new ButtonGroup();

		final CardLayout pathLayout = new CardLayout();
		final JPanel pathPanel = new JPanel(pathLayout);

		pathPanel.add(JBUI.Panels.verticalPanel().addComponent(createPathComponent(true)), "directory");
		pathPanel.add(JBUI.Panels.verticalPanel().addComponent(createPathComponent(false)), "file");

		ItemListener listener = new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				pathLayout.show(pathPanel, myDirectoryButton.isSelected() ? "directory" : "file");
			}
		};

		myDirectoryButton = new JRadioButton("Directory");
		myDirectoryButton.addItemListener(listener);

		myFileButton = new JRadioButton("File");
		myFileButton.addItemListener(listener);

		targetGroup.add(myDirectoryButton);
		targetGroup.add(myFileButton);

		JPanel panel = new JPanel(new HorizontalLayout());
		panel.add(new JBLabel("Test in:"));
		panel.add(myDirectoryButton);
		panel.add(myFileButton);

		add(panel);
		add(pathPanel);

		getProgramParametersComponent().setVisible(false);
	}

	private JComponent createPathComponent(boolean directory)
	{
		String label = directory ? "Directory" : "File";
		TextFieldWithBrowseButton textField = new TextFieldWithBrowseButton();
		textField.addBrowseFolderListener("Select Script", "Select Script " + label, myProject, new FileChooserDescriptor(!directory, directory, false, false, false, false),
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
		if(directory)
		{
			myDirectoryField = textField;
		}
		else
		{
			myFileField = textField;
		}
		return LabeledComponent.left(textField, label);
	}

	@RequiredDispatchThread
	@Override
	public void reset(CommonProgramRunConfigurationParameters configuration)
	{
		super.reset(configuration);

		MochaConfiguration mochaConfiguration = (MochaConfiguration) configuration;

		switch(mochaConfiguration.getTargetType())
		{
			case DIRECTORY:
				myDirectoryButton.setSelected(true);
				break;
			case FILE:
				myFileButton.setSelected(true);
				break;
		}

		myDirectoryField.setText(mochaConfiguration.getDirectoryPath());
		myFileField.setText(mochaConfiguration.getFilePath());
	}

	@RequiredDispatchThread
	@Override
	public void applyTo(CommonProgramRunConfigurationParameters configuration)
	{
		super.applyTo(configuration);

		MochaConfiguration mochaConfiguration = (MochaConfiguration) configuration;

		if(myDirectoryButton.isSelected())
		{
			mochaConfiguration.setTargetType(MochaConfiguration.TargetType.DIRECTORY);
			mochaConfiguration.setFilePath(null);
			mochaConfiguration.setDirectoryPath(myDirectoryField.getText());
		}
		else
		{
			mochaConfiguration.setTargetType(MochaConfiguration.TargetType.FILE);
			mochaConfiguration.setFilePath(myFileField.getText());
			mochaConfiguration.setDirectoryPath(null);
		}
	}
}
