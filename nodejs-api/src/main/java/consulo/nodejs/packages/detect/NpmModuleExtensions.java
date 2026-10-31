package consulo.nodejs.packages.detect;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.content.bundle.SdkType;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.extension.MutableModuleExtension;
import consulo.module.extension.MutableModuleExtensionWithSdk;
import consulo.nodejs.npm.NpmPackage;
import consulo.nodejs.npm.NpmPackageParser;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The module extensions of the frameworks a {@code package.json} depends on ({@link NpmModuleExtensionDetector}).
 */
public final class NpmModuleExtensions {
    public static final String PACKAGE_JSON = "package.json";

    private NpmModuleExtensions() {
    }

    /**
     * The names of the dependencies (also dev, peer and optional) of a {@code package.json}.
     */
    public static Set<String> getDependencyNames(@Nullable VirtualFile packageJson) {
        NpmPackage npmPackage = NpmPackageParser.parse(packageJson);
        return npmPackage == null ? Set.of() : npmPackage.getAllDependencyNames();
    }

    public static Set<String> getDependencyNames(String packageJsonText) {
        NpmPackage npmPackage = NpmPackageParser.parse(packageJsonText);
        return npmPackage == null ? Set.of() : npmPackage.getAllDependencyNames();
    }

    /**
     * The detectors of the frameworks the {@code package.json} of a directory depends on.
     */
    public static List<NpmModuleExtensionDetector> detect(@Nullable VirtualFile directory) {
        Set<String> dependencies = getDependencyNames(directory != null ? directory.findChild(PACKAGE_JSON) : null);
        if (dependencies.isEmpty()) {
            return List.of();
        }
        List<NpmModuleExtensionDetector> result = new ArrayList<>();
        for (NpmModuleExtensionDetector detector : Application.get().getExtensionList(NpmModuleExtensionDetector.class)) {
            if (!Collections.disjoint(detector.getPackageNames(), dependencies)) {
                result.add(detector);
            }
        }
        return result;
    }

    /**
     * The detected frameworks of the content roots of a module whose extension isn't enabled.
     */
    @RequiredReadAction
    public static List<NpmModuleExtensionDetector> findMissing(Module module) {
        List<NpmModuleExtensionDetector> result = new ArrayList<>();
        for (VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
            for (NpmModuleExtensionDetector detector : detect(root)) {
                if (module.getExtension(detector.getExtensionId()) == null && !result.contains(detector)) {
                    result.add(detector);
                }
            }
        }
        return result;
    }

    /**
     * Enables the extensions of the frameworks the {@code package.json} of {@code directory} depends on.
     */
    public static void enableDetected(ModifiableRootModel model, @Nullable VirtualFile directory) {
        for (NpmModuleExtensionDetector detector : detect(directory)) {
            enableWithParents(model, detector.getExtensionId());
            if (directory != null) {
                detector.configure(model, directory);
            }
        }
    }

    /**
     * Enables a module extension and its parents ({@link ModuleExtensionProvider#getParentId()}).
     */
    @SuppressWarnings("rawtypes")
    public static void enableWithParents(ModifiableRootModel model, String extensionId) {
        for (String id = extensionId; id != null; ) {
            MutableModuleExtension extension = model.getExtensionWithoutCheck(id);
            if (extension != null) {
                extension.setEnabled(true);
                if (extension instanceof MutableModuleExtensionWithSdk withSdk) {
                    attachSdk(model, withSdk);
                }
            }
            ModuleExtensionProvider provider = ModuleExtensionProvider.findProvider(id);
            id = provider != null ? provider.getParentId() : null;
        }
    }

    /**
     * An extension with an SDK (Node.js, Web) gets one if it has none: the predefined SDK of its type, else the first
     * one; and its order entry - the roots of the SDK (the standard library) are indexed and in the resolve scope only
     * with it.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void attachSdk(ModifiableRootModel model, MutableModuleExtensionWithSdk extension) {
        if (extension.getInheritableSdk().isNull()) {
            Sdk sdk = findSdk(sdkTypeClass(extension));
            if (sdk != null) {
                extension.getInheritableSdk().set(null, sdk);
            }
        }
        if (!extension.getInheritableSdk().isNull() && model.findModuleExtensionSdkEntry(extension) == null) {
            model.addModuleExtensionSdkEntry(extension);
        }
    }

    /**
     * The SDK type of the extension, {@code null} for an extension without one of its own (Vaadin Flow throws)
     */
    @SuppressWarnings("rawtypes")
    private static @Nullable Class<? extends SdkType> sdkTypeClass(MutableModuleExtensionWithSdk extension) {
        try {
            return extension.getSdkTypeClass();
        }
        catch (UnsupportedOperationException | IllegalArgumentException e) {
            return null;
        }
    }

    private static @Nullable Sdk findSdk(@Nullable Class<? extends SdkType> sdkTypeClass) {
        if (sdkTypeClass == null) {
            return null;
        }
        SdkType sdkType = Application.get().getExtensionPoint(SdkType.class).findExtension(sdkTypeClass);
        if (sdkType == null) {
            return null;
        }
        SdkTable sdkTable = SdkTable.getInstance();
        Sdk predefined = sdkTable.findPredefinedSdkByType(sdkType);
        if (predefined != null) {
            return predefined;
        }
        List<Sdk> sdks = sdkTable.getSdksOfType(sdkType);
        return sdks.isEmpty() ? null : sdks.get(0);
    }
}
