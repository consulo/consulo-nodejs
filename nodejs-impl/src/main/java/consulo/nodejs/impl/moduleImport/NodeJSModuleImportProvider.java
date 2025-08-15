package consulo.nodejs.impl.moduleImport;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.WriteAction;
import consulo.content.base.ExcludedContentFolderTypeProvider;
import consulo.ide.moduleImport.ModuleImportProvider;
import consulo.localize.LocalizeValue;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.nodejs.impl.module.extension.NodeJSMutableModuleExtension;
import consulo.nodejs.impl.newProjectOrModule.ui.NodeJSNewModuleSetupStep;
import consulo.project.Project;
import consulo.ui.ex.wizard.WizardStep;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 05/12/2020
 */
@ExtensionImpl
public class NodeJSModuleImportProvider implements ModuleImportProvider<NodeJSModuleImportContext> {
    @Nonnull
    @Override
    public LocalizeValue getName() {
        return LocalizeValue.localizeTODO("Node.js");
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return NodeJSApiIconGroup.nodejs();
    }

    @Override
    public boolean canImport(@Nonnull File file) {
        return new File(file, "package.json").exists();
    }

    @Nonnull
    @Override
    public NodeJSModuleImportContext createContext(@Nullable Project project) {
        return new NodeJSModuleImportContext(project);
    }

    @Override
    public void buildSteps(@Nonnull Consumer<WizardStep<NodeJSModuleImportContext>> consumer, @Nonnull NodeJSModuleImportContext context) {
        consumer.accept(new NodeJSNewModuleSetupStep<>(context));
    }

    @RequiredReadAction
    @Override
    public void process(@Nonnull NodeJSModuleImportContext context,
                        @Nonnull Project project,
                        @Nonnull ModifiableModuleModel modifiableModuleModel,
                        @Nonnull Consumer<Module> consumer) {
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
