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
					String relativePath = FileUtil.getRelativePath(moduleDirPath, FileUtil.toSystemIndependentName(text), '/');
					jTextField.setText(relativePath);
				}
			}
		});
	}

	@Override
	protected void resetEditorFrom(NodeJSConfiguration nodeJSConfiguration)
	{
		myScriptTextField.setText(nodeJSConfiguration.getScriptName());
		myModuleBox.setSelectedItem(nodeJSConfiguration.getConfigurationModule().getModule());
	}

	@Override
	protected void applyEditorTo(NodeJSConfiguration nodeJSConfiguration) throws ConfigurationException
	{
		nodeJSConfiguration.setScriptName(myScriptTextField.getText());
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
