package consulo.nodejs.impl.run;

import consulo.content.bundle.Sdk;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunConfiguration;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.ui.SettingsEditor;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.nodejs.run.NodeJSConfigurationBase;
import consulo.nodejs.run.NodeJSRunState;
import consulo.process.ExecutionException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
public class NpxConfiguration extends NodeJSConfigurationBase
{
	private String myNpxCommand;

	public NpxConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	@Nonnull
	@Override
	public List<Module> getValidModules()
	{
		List<Module> list = new ArrayList<>();
		for(Module module : ModuleManager.getInstance(getProject()).getModules())
		{
			if(ModuleUtilCore.getExtension(module, NodeJSModuleExtension.class) != null)
			{
				list.add(module);
			}
		}
		return list;
	}

	@Nonnull
	@Override
	protected NodeJSRunState createRunState(@Nullable Module module, @Nonnull Sdk targetSdk, @Nonnull Executor executor, @Nonnull ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		NpxRunState npxRunState = new NpxRunState(module, targetSdk, this);
		npxRunState.addVmArgument(myNpxCommand);
		return npxRunState;
	}

	@Nonnull
	@Override
	public SettingsEditor<? extends RunConfiguration> getConfigurationEditor()
	{
		return new NpxConfigurationSettingsEditor(getProject());
	}

	public String getNpxCommand()
	{
		return myNpxCommand;
	}

	public void setNpxCommand(String npxCommand)
	{
		myNpxCommand = npxCommand;
	}
}
