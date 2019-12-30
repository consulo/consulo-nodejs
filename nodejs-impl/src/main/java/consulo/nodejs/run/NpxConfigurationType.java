package consulo.nodejs.run;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.openapi.project.Project;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.nodejs.NodeJSIcons;
import consulo.nodejs.module.extension.NodeJSModuleExtension;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
public class NpxConfigurationType extends ConfigurationTypeBase
{
	public NpxConfigurationType()
	{
		super("NpxConfigurationType", "Npx", "", NodeJSIcons.Npm);

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
