package consulo.nodejs.impl.run;

import consulo.content.bundle.Sdk;
import consulo.module.Module;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.nodejs.run.NodeJSConfigurationBase;
import consulo.nodejs.run.NodeJSRunState;
import consulo.process.cmd.GeneralCommandLine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
public class NpxRunState extends NodeJSRunState
{
	public NpxRunState(@Nullable Module module, @Nonnull Sdk sdk, NodeJSConfigurationBase configuration)
	{
		super(module, sdk, configuration);
	}

	@Override
	protected void setupExePath(@Nonnull GeneralCommandLine commandLine, @Nonnull Sdk sdk)
	{
		commandLine.setExePath(NodeJSBundleType.getExePath(sdk.getHomePath(), "npx.cmd", "npx").getPath());
	}
}
