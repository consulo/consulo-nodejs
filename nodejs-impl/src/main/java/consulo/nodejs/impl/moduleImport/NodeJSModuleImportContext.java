package consulo.nodejs.impl.moduleImport;

import consulo.content.bundle.Sdk;
import consulo.ide.moduleImport.ModuleImportContext;
import consulo.nodejs.impl.newProjectOrModule.NodeJSNewModuleWizardContext;
import consulo.project.Project;

import jakarta.annotation.Nullable;

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
