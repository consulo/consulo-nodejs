package consulo.nodejs.impl.run;

import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.content.bundle.Sdk;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.nodejs.run.NodeJSConfigurationBase;
import consulo.nodejs.run.NodeJSRunState;
import consulo.process.ExecutionException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
public class NpxConfiguration extends NodeJSConfigurationBase
{

	public NpxConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory)
	{
		super(name, configurationModule, factory);
	}

	@Override
	protected Class<NpxConfigurationOptions> getOptionsClass()
	{
		return NpxConfigurationOptions.class;
	}

	@Override
	public NpxConfigurationOptions getOptions()
	{
		return (NpxConfigurationOptions) super.getOptions();
	}

	@Override
	protected void convertLegacyState(LegacyRunConfigurationConverter converter)
	{
		super.convertLegacyState(converter);

		NpxConfigurationOptions options = new NpxConfigurationOptions();
		convertLegacyCommonState(converter, options);
		NpxConfigurationLegacyState old = converter.readXml(NpxConfigurationLegacyState.class);
		options.setNpxCommand(old.npxCommand);
		converter.setOptions(options);
	}

	@Override
	public List<RunConfigurationFragment<?>> createFragments()
	{
		List<RunConfigurationFragment<?>> fragments = new ArrayList<>();
		fragments.add(new NpxCommandFragment());
		fragments.addAll(createCommonFragments());
		return fragments;
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
		npxRunState.addVmArgument(getNpxCommand());
		return npxRunState;
	}


	public String getNpxCommand()
	{
		return getOptions().getNpxCommand();
	}

	public void setNpxCommand(String npxCommand)
	{
		getOptions().setNpxCommand(npxCommand);
	}
}
