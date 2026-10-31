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
package consulo.nodejs.impl.npm;

import consulo.annotation.component.ExtensionImpl;
import consulo.content.library.Library;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.orderEntry.LibraryOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.nodejs.npm.NpmConstants;
import consulo.nodejs.npm.NpmProjectModel;
import consulo.project.Project;
import consulo.project.ProjectManager;
import consulo.virtualFileSystem.event.AsyncFileListener;
import consulo.virtualFileSystem.event.VFileContentChangeEvent;
import consulo.virtualFileSystem.event.VFileCreateEvent;
import consulo.virtualFileSystem.event.VFileDeleteEvent;
import consulo.virtualFileSystem.event.VFileEvent;
import consulo.virtualFileSystem.event.VFileMoveEvent;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps the npm part of a module in sync ({@link NpmProjectModel#scheduleSync}) when
 * <ul>
 * <li>its {@code package.json} changes - for the modules not imported from a linked npm package (a Maven module with a
 * frontend), a linked package is imported again by the platform;</li>
 * <li>a package is installed or removed in its {@code node_modules}: the excluded directories of the libraries follow
 * the installed packages.</li>
 * </ul>
 *
 * @since 2026-10-31
 */
@ExtensionImpl
public class NpmPackageJsonListener implements AsyncFileListener {
    private final ProjectManager myProjectManager;

    @Inject
    public NpmPackageJsonListener(ProjectManager projectManager) {
        myProjectManager = projectManager;
    }

    @Override
    public @Nullable ChangeApplier prepareChange(List<? extends VFileEvent> events) {
        // the package directory -> whether the package.json changed (else only node_modules)
        Map<Path, Boolean> packageDirectories = new LinkedHashMap<>();
        for (VFileEvent event : events) {
            if (!(event instanceof VFileContentChangeEvent || event instanceof VFileCreateEvent
                || event instanceof VFileDeleteEvent || event instanceof VFileMoveEvent)) {
                continue;
            }
            Path path;
            try {
                path = Path.of(event.getPath());
            }
            catch (RuntimeException e) {
                continue;
            }
            Path fileName = path.getFileName();
            Path parent = path.getParent();
            if (fileName == null || parent == null) {
                continue;
            }
            if (NpmConstants.PACKAGE_JSON.equals(fileName.toString())) {
                packageDirectories.put(parent, Boolean.TRUE);
                continue;
            }
            Path packageDirectory = findPackageDirectoryOfInstalled(path);
            if (packageDirectory != null) {
                packageDirectories.putIfAbsent(packageDirectory, Boolean.FALSE);
            }
        }
        if (packageDirectories.isEmpty()) {
            return null;
        }
        return new ChangeApplier() {
            @Override
            public void afterVfsChange() {
                for (Project project : myProjectManager.getOpenProjects()) {
                    if (project.isDisposed()) {
                        continue;
                    }
                    for (Map.Entry<Path, Boolean> entry : packageDirectories.entrySet()) {
                        Module module = NpmUtil.findModule(project, entry.getKey());
                        if (module != null && isSynced(module, entry.getValue())) {
                            NpmProjectModel.getInstance(project).scheduleSync(module);
                        }
                    }
                }
            }
        };
    }

    /**
     * @return the package directory of {@code <package>/node_modules/<name>} or {@code <package>/node_modules/@scope/<name>}
     */
    private static @Nullable Path findPackageDirectoryOfInstalled(Path path) {
        Path parent = path.getParent();
        if (parent == null) {
            return null;
        }
        if (isNodeModules(parent)) {
            return parent.getParent();
        }
        Path scope = parent.getFileName();
        Path grandParent = parent.getParent();
        if (scope != null && scope.toString().startsWith("@") && grandParent != null && isNodeModules(grandParent)) {
            return grandParent.getParent();
        }
        return null;
    }

    private static boolean isNodeModules(Path path) {
        Path fileName = path.getFileName();
        return fileName != null && NpmConstants.NODE_MODULES.equals(fileName.toString());
    }

    /**
     * @param packageJsonChanged whether the package.json changed: a linked package is imported again by the platform
     */
    private static boolean isSynced(Module module, boolean packageJsonChanged) {
        if (packageJsonChanged && NpmUtil.getForeignOwner(module) == null && NpmUtil.isLinked(module)) {
            return false;
        }
        return NpmUtil.getForeignOwner(module) != null
            || module.getExtension(NodeJSModuleExtension.class) != null
            || hasNpmLibraries(module);
    }

    private static boolean hasNpmLibraries(Module module) {
        for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry libraryOrderEntry) {
                Library library = libraryOrderEntry.getLibrary();
                String name = library == null ? null : library.getName();
                if (name != null && name.startsWith(NpmConstants.LIBRARY_PREFIX)) {
                    return true;
                }
            }
        }
        return false;
    }
}
