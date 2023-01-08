package consulo.nodejs.impl.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationTypeBase;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.project.Project;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
@ExtensionImpl
public class NpxConfigurationType extends ConfigurationTypeBase
{
	public NpxConfigurationType()
	{
		super("NpxConfigurationType", "Npx", "", NodeJSApiIconGroup.npm());

		addFactory(new ConfigurationFactory(this)
		{
			@Override
			public RunConfiguration createTemplateConfiguration(Project project)
			{
				return new NpxConfiguration("Unnamed", new RunConfigurationModule(project), this);
			}

			@Override
			public boolean isApplicable(@Nonnull Project project)
			{
				return ModuleExtensionHelper.getInstance(project).hasModuleExtension(NodeJSModuleExtension.class);
			}
		});
	}
}
