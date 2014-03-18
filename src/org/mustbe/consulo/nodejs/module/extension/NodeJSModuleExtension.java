package org.mustbe.consulo.nodejs.module.extension;

import org.consulo.module.extension.ModuleExtensionWithSdk;
import org.consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.nodejs.bundle.NodeJSBundleType;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * @author VISTALL
 * @since 14.03.14
 */
public class NodeJSModuleExtension extends ModuleExtensionWithSdkImpl<NodeJSModuleExtension> implements ModuleExtensionWithSdk<NodeJSModuleExtension>
{
	public NodeJSModuleExtension(@NotNull String id, @NotNull ModifiableRootModel rootModel)
	{
		super(id, rootModel);
	}

	@Override
	protected Class<? extends SdkType> getSdkTypeClass()
	{
		return NodeJSBundleType.class;
	}
}
