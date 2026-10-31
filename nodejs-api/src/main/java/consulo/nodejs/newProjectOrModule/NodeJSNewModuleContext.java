package consulo.nodejs.newProjectOrModule;

import consulo.content.bundle.Sdk;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.creation.NewModuleWizardContextBase;
import consulo.module.extension.MutableModuleExtensionWithSdk;
import consulo.nodejs.packages.detect.NpmModuleExtensions;

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

	/**
	 * Enables the Node.js extension of the new module with the chosen bundle (for builders of other plugins: the
	 * extension class is in the implementation).
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void setupNodeJS(ModifiableRootModel modifiableRootModel)
	{
		MutableModuleExtensionWithSdk extension = modifiableRootModel.getExtensionWithoutCheck("nodejs");
		if(extension == null)
		{
			return;
		}
		extension.setEnabled(true);
		if(mySdk != null)
		{
			extension.getInheritableSdk().set(null, mySdk);
			modifiableRootModel.addModuleExtensionSdkEntry(extension);
		}
	}

	public static final String WEB_EXTENSION_ID = "client-javascript";

	/**
	 * Enables the Web extension of the new module (the browser libraries: an application of Vite, Vue.js, Angular runs
	 * in a browser, Node.js alone gives the ECMAScript library only), and the framework extensions {@code extensionIds}
	 * with their parents.
	 */
	public void setupWeb(ModifiableRootModel modifiableRootModel, String... extensionIds)
	{
		NpmModuleExtensions.enableWithParents(modifiableRootModel, WEB_EXTENSION_ID);
		for(String extensionId : extensionIds)
		{
			NpmModuleExtensions.enableWithParents(modifiableRootModel, extensionId);
		}
	}
}
