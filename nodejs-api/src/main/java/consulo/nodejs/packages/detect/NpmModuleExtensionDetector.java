package consulo.nodejs.packages.detect;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.virtualFileSystem.VirtualFile;

import java.util.Set;

/**
 * A module extension of a framework used through npm (Vue.js, Angular): enabled when a {@code package.json} of the
 * module depends on one of {@link #getPackageNames()} — on import of the module, and through the editor notification
 * of the modules that don't have it yet ({@link NpmModuleExtensions}).
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface NpmModuleExtensionDetector {
    /**
     * The id of the module extension (its parents are enabled with it).
     */
    String getExtensionId();

    /**
     * The npm packages whose use means the framework ({@code vue}, {@code nuxt}).
     */
    Set<String> getPackageNames();

    /**
     * The files whose editors offer to enable the extension: the {@code package.json} by default.
     */
    default boolean isRelevantFile(VirtualFile file) {
        return NpmModuleExtensions.PACKAGE_JSON.equals(file.getName());
    }
}
