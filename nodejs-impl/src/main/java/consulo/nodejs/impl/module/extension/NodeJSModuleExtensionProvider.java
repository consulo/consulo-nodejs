package consulo.nodejs.impl.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 08/01/2023
 */
@ExtensionImpl
public class NodeJSModuleExtensionProvider implements ModuleExtensionProvider<NodeJSModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "nodejs";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of("Node.js");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return NodeJSApiIconGroup.nodejs();
	}

	@Nonnull
	@Override
	public ModuleExtension<NodeJSModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new NodeJSModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<NodeJSModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new NodeJSMutableModuleExtension(getId(), moduleRootLayer);
	}
}
