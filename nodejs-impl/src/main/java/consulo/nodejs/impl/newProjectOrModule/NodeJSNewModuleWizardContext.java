package consulo.nodejs.impl.newProjectOrModule;

import consulo.content.bundle.Sdk;
import consulo.ide.newModule.NewModuleWizardContext;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 05/12/2020
 */
public interface NodeJSNewModuleWizardContext extends NewModuleWizardContext
{
	void setSdk(@Nullable Sdk sdk);

	@Nullable
	Sdk getSdk();
}
