package consulo.nodejs.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.projectRoots.Sdk;
import consulo.nodejs.module.extension.NodeJSModuleExtension;

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
