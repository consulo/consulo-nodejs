package consulo.nodejs.newProjectOrModule;

import com.intellij.openapi.projectRoots.Sdk;
import consulo.ide.wizard.newModule.NewModuleWizardContext;

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
