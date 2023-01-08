package consulo.mocha.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.mocha.icon.MochaIconGroup;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 08/01/2023
 */
@ExtensionImpl
public class MochaModuleExtensionProvider implements ModuleExtensionProvider<MochaModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "mocha";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return "nodejs";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.of("Mocha");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return MochaIconGroup.mocha();
	}

	@Nonnull
	@Override
	public ModuleExtension<MochaModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MochaModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<MochaModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MochaMutableModuleExtension(getId(), moduleRootLayer);
	}
}
