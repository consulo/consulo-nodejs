package consulo.nodejs.run;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;
import javax.swing.*;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
public class NpxConfigurationSettingsEditor extends SettingsEditor<NpxConfiguration>
{
	private final Project myProject;
	private NpxConfigurationPanel myConfigurationPanel;

	public NpxConfigurationSettingsEditor(Project project)
	{
		myProject = project;
	}

	@Override
	@RequiredUIAccess
	protected void resetEditorFrom(NpxConfiguration npxConfiguration)
	{
		myConfigurationPanel.reset(npxConfiguration);
	}

	@Override
	@RequiredUIAccess
	protected void applyEditorTo(NpxConfiguration npxConfiguration) throws ConfigurationException
	{
		myConfigurationPanel.applyTo(npxConfiguration);
	}

	@Nonnull
	@Override
	protected JComponent createEditor()
	{
		if(myConfigurationPanel == null)
		{
			myConfigurationPanel = new NpxConfigurationPanel(myProject);
		}
		return myConfigurationPanel;
	}
}
