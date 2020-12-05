package consulo.nodejs.newProjectOrModule;

import com.intellij.openapi.projectRoots.Sdk;
import consulo.ide.wizard.newModule.NewModuleWizardContextBase;

/**
 * @author VISTALL
 * @since 2019-09-06
 */
public class NodeJSNewModuleContext extends NewModuleWizardContextBase implements NodeJSNewModuleWizardContext
{
	private Sdk mySdk;

	public NodeJSNewModuleContext(boolean isNewProject)
	{
		super(isNewProject);
	}

	@Override
	public void setSdk(Sdk sdk)
	{
		mySdk = sdk;
	}

	@Override
	public Sdk getSdk()
	{
		return mySdk;
	}
}
