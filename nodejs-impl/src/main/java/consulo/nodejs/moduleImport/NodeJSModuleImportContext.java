package consulo.nodejs.moduleImport;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import consulo.moduleImport.ModuleImportContext;
import consulo.nodejs.newProjectOrModule.NodeJSNewModuleWizardContext;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 05/12/2020
 */
public class NodeJSModuleImportContext extends ModuleImportContext implements NodeJSNewModuleWizardContext
{
	private Sdk mySdk;

	public NodeJSModuleImportContext(@Nullable Project project)
	{
		super(project);
	}

	@Override
	public void setSdk(@Nullable Sdk sdk)
	{
		mySdk = sdk;
	}

	@Nullable
	@Override
	public Sdk getSdk()
	{
		return mySdk;
	}
}
