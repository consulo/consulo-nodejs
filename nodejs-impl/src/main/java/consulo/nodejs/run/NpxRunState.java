package consulo.nodejs.run;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import consulo.nodejs.bundle.NodeJSBundleType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-12-30
 */
public class NpxRunState extends NodeJSRunState
{
	public NpxRunState(@Nonnull Module module, @Nonnull Sdk sdk, NodeJSConfigurationBase configuration)
	{
		super(module, sdk, configuration);
	}

	@Override
	protected void setupExePath(@Nonnull GeneralCommandLine commandLine, @Nonnull Sdk sdk)
	{
		commandLine.setExePath(NodeJSBundleType.getExePath(sdk.getHomePath(), "npx.cmd", "npx").getPath());
	}
}
