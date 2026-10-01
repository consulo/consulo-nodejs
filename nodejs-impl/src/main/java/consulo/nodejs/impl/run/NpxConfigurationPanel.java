package consulo.nodejs.impl.run;

import consulo.disposer.Disposable;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.nodejs.run.NodeJSConfigurationPanelBase;
import consulo.project.Project;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
public class NpxConfigurationPanel extends NodeJSConfigurationPanelBase<NpxConfiguration>
{
	private final TextBox myCommandField;

	@RequiredUIAccess
	public NpxConfigurationPanel(Project project, Disposable uiDisposable)
	{
		super(project, uiDisposable);

		myCommandField = TextBox.create();
	}

	@Override
	@RequiredUIAccess
	protected void addBefore(FormBuilder builder)
	{
		builder.addLabeled(NodeJSLocalize.runConfigurationCommandLabel(), myCommandField);
	}

	@Override
	@RequiredUIAccess
	protected void addAfter(FormBuilder builder)
	{
		addModuleAndBundle(builder);
	}

	@Override
	@RequiredUIAccess
	public void apply(NpxConfiguration configuration)
	{
		super.apply(configuration);

		configuration.setNpxCommand(myCommandField.getValue());
	}

	@Override
	@RequiredUIAccess
	public void reset(NpxConfiguration configuration)
	{
		super.reset(configuration);

		myCommandField.setValue(StringUtil.notNullize(configuration.getNpxCommand()));
	}
}
