/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.nodejs.npm;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.module.Module;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

/**
 * Extends the model of a module with its {@code package.json}:
 * <ul>
 * <li>the Node.js extension, and the extensions of the frameworks of the dependencies ({@link NpmImporter}) - only
 * enabled, never disabled;</li>
 * <li>a module library per dependency ({@link NpmConstants#LIBRARY_PREFIX}{@code <name>}) over
 * {@code node_modules/<name>}, without the tests and docs of the package; dev dependencies have the test scope.</li>
 * </ul>
 * Nothing else of the module is touched, so it works for the modules owned by another build system too: a Maven module
 * mixing Java and a frontend (Vaadin) calls {@link #apply} from its importer, or {@link #scheduleSync} after it.
 *
 * @since 2026-10-31
 */
@ServiceAPI(ComponentScope.PROJECT)
public interface NpmProjectModel {
    static NpmProjectModel getInstance(Project project) {
        return project.getInstance(NpmProjectModel.class);
    }

    /**
     * Changes the model owned by the caller (it commits or disposes it).
     *
     * @param packageRoot the directory of the {@code package.json}
     * @return whether the model was changed
     */
    @RequiredReadAction
    boolean apply(ModifiableRootModel model, VirtualFile packageRoot, NpmPackage npmPackage);

    /**
     * Reads the {@code package.json} of the module and applies it to the model of the module, in the background.
     */
    void scheduleSync(Module module);

    /**
     * @return the {@code package.json} of the module: in one of its content roots
     */
    @RequiredReadAction
    @Nullable VirtualFile findPackageJson(Module module);
}
