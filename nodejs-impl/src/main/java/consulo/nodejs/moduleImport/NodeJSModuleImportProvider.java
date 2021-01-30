package consulo.nodejs.moduleImport;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.moduleImport.ModuleImportProvider;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.nodejs.module.extension.NodeJSMutableModuleExtension;
import consulo.nodejs.newProjectOrModule.ui.NodeJSNewModuleSetupStep;
import consulo.roots.impl.ExcludedContentFolderTypeProvider;
import consulo.ui.image.Image;
import consulo.ui.wizard.WizardStep;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 05/12/2020
 */
public class NodeJSModuleImportProvider implements ModuleImportProvider<NodeJSModuleImportContext>
{
	@Nonnull
	@Override
	public String getName()
	{
		return "Node.js";
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return NodeJSApiIconGroup.nodejs();
	}

	@Override
	public boolean canImport(@Nonnull File file)
	{
		return new File(file, "package.json").exists();
	}

	@Nonnull
	@Override
	public NodeJSModuleImportContext createContext(@Nullable Project project)
	{
		return new NodeJSModuleImportContext(project);
	}

	@Override
	public void buildSteps(@Nonnull Consumer<WizardStep<NodeJSModuleImportContext>> consumer, @Nonnull NodeJSModuleImportContext context)
	{
		consumer.accept(new NodeJSNewModuleSetupStep<>(context));
	}

	@RequiredReadAction
	@Override
	public void process(@Nonnull NodeJSModuleImportContext context,
						@Nonnull Project project,
						@Nonnull ModifiableModuleModel modifiableModuleModel,
						@Nonnull Consumer<Module> consumer)
	{
		File dir = new File(context.getPath());

		VirtualFile localDir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dir);

		Module module = modifiableModuleModel.newModule(dir.getName(), dir.getPath());

		ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);

		ModifiableRootModel modifiableModel = moduleRootManager.getModifiableModel();

		ContentEntry contentEntry = modifiableModel.addContentEntry(localDir);

		contentEntry.addFolder(localDir.getUrl() + "/node_modules", ExcludedContentFolderTypeProvider.getInstance());

		NodeJSMutableModuleExtension extension = modifiableModel.getExtensionWithoutCheck(NodeJSMutableModuleExtension.class);
		extension.setEnabled(true);
		extension.getInheritableSdk().set(null, context.getSdk());

		WriteAction.runAndWait(modifiableModel::commit);
	}
}
