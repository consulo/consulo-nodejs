package consulo.nodejs.impl.run;

import consulo.configurable.ConfigurationException;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
public class NpxConfigurationSettingsEditor extends SettingsEditor<NpxConfiguration>
{
	private final Project myProject;
	@Nullable
	private NpxConfigurationPanel myConfigurationPanel;

	public NpxConfigurationSettingsEditor(Project project)
	{
		myProject = project;
	}

	@Override
	@RequiredUIAccess
	protected void resetEditorFrom(NpxConfiguration npxConfiguration)
	{
		NpxConfigurationPanel configurationPanel = myConfigurationPanel;
		if(configurationPanel != null)
		{
			configurationPanel.reset(npxConfiguration);
		}
	}

	@Override
	@RequiredUIAccess
	protected void applyEditorTo(NpxConfiguration npxConfiguration) throws ConfigurationException
	{
		NpxConfigurationPanel configurationPanel = myConfigurationPanel;
		if(configurationPanel != null)
		{
			configurationPanel.apply(npxConfiguration);
		}
	}

	@Override
	@RequiredUIAccess
	protected Component createUIComponent()
	{
		NpxConfigurationPanel configurationPanel = new NpxConfigurationPanel(myProject, this);
		myConfigurationPanel = configurationPanel;
		configurationPanel.build();
		return configurationPanel.getComponent();
	}
}
