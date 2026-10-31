package consulo.nodejs.impl.packages.detect;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.WriteAction;
import consulo.fileEditor.EditorNotificationBuilder;
import consulo.fileEditor.EditorNotificationProvider;
import consulo.fileEditor.EditorNotifications;
import consulo.fileEditor.FileEditor;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.nodejs.packages.detect.NpmModuleExtensionDetector;
import consulo.nodejs.packages.detect.NpmModuleExtensions;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Offers to enable the module extension of a framework the {@code package.json} of the module depends on
 * ({@link NpmModuleExtensionDetector}) in the editors of its relevant files.
 */
@ExtensionImpl
public class NpmModuleExtensionNotificationProvider implements EditorNotificationProvider {
    private final Project myProject;

    @Inject
    public NpmModuleExtensionNotificationProvider(Project project) {
        myProject = project;
    }

    @Override
    public String getId() {
        return "npm-module-extension";
    }

    @Nullable
    @Override
    @RequiredReadAction
    public EditorNotificationBuilder buildNotification(VirtualFile file, FileEditor fileEditor, Supplier<EditorNotificationBuilder> builderFactory) {
        Module module = ModuleUtilCore.findModuleForFile(file, myProject);
        if (module == null) {
            return null;
        }
        List<NpmModuleExtensionDetector> missing = NpmModuleExtensions.findMissing(module).stream()
            .filter(detector -> detector.isRelevantFile(file))
            .toList();
        if (missing.isEmpty()) {
            return null;
        }
        NpmModuleExtensionDetector detector = missing.get(0);
        ModuleExtensionProvider provider = ModuleExtensionProvider.findProvider(detector.getExtensionId());
        LocalizeValue name = provider != null ? provider.getName() : LocalizeValue.of(detector.getExtensionId());
        EditorNotificationBuilder builder = builderFactory.get();
        builder.withText(NodeJSLocalize.npmFrameworkDetectedNotification(name));
        builder.withAction(NodeJSLocalize.npmFrameworkDetectedEnableAction(name), event -> {
            WriteAction.run(() -> {
                ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
                NpmModuleExtensions.enableWithParents(model, detector.getExtensionId());
                model.commit();
            });
            EditorNotifications.getInstance(myProject).updateAllNotifications();
        });
        return builder;
    }
}
