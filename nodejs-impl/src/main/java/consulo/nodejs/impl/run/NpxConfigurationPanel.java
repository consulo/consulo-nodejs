package consulo.nodejs.impl.run;

import consulo.annotation.access.RequiredReadAction;
import consulo.execution.CommonProgramRunConfigurationParameters;
import consulo.nodejs.run.NodeJSConfigurationPanelBase;
import consulo.project.Project;
import consulo.ui.ex.awt.JBTextField;
import consulo.ui.ex.awt.JBUI;
import consulo.ui.ex.awt.LabeledComponent;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
public class NpxConfigurationPanel extends NodeJSConfigurationPanelBase
{
	private JBTextField myCommandField;

	public NpxConfigurationPanel(Project project)
	{
		super(project);
	}

	@Override
	protected void initComponents()
	{
		myCommandField = new JBTextField();

		super.initComponents();
	}

	@Override
	protected void addComponents()
	{
		add(LabeledComponent.create(myCommandField, "Command"));
		addComponentsInternal();
		add(LabeledComponent.create(myModuleBox, "Module"));
		add(JBUI.Panels.simplePanel().addToLeft(myUseAlternativeBundleCheckBox).addToCenter(myAlternativeBundleComboBox));
	}

	@Override
	@RequiredReadAction
	public void applyTo(CommonProgramRunConfigurationParameters configuration)
	{
		super.applyTo(configuration);

		NpxConfiguration npxConfiguration = (NpxConfiguration) configuration;

		npxConfiguration.setNpxCommand(myCommandField.getText());
	}

	@Override
	@RequiredReadAction
	public void reset(CommonProgramRunConfigurationParameters configuration)
	{
		super.reset(configuration);

		NpxConfiguration npxConfiguration = (NpxConfiguration) configuration;

		myCommandField.setText(npxConfiguration.getNpxCommand());
	}
}

