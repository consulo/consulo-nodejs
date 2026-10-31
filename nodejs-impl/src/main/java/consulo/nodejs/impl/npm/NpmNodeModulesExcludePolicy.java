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
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.DirectoryIndexExcludePolicy;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.orderEntry.LibraryOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.nodejs.npm.NpmConstants;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.pointer.LightFilePointer;
import consulo.virtualFileSystem.pointer.VirtualFilePointer;

import java.util.ArrayList;
import java.util.List;

/**
 * Excludes the {@code node_modules} of a module with npm libraries without storing it in the module: another build
 * system owning the module (Maven) sets its folders again on each import. The packages of the dependencies stay
 * visible as libraries.
 *
 * @since 2026-10-31
 */
@ExtensionImpl
public class NpmNodeModulesExcludePolicy implements DirectoryIndexExcludePolicy {
    @Override
    public VirtualFile[] getExcludeRootsForProject() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    public VirtualFilePointer[] getExcludeRootsForModule(ModuleRootLayer moduleRootLayer) {
        if (!hasNpmLibraries(moduleRootLayer)) {
            return VirtualFilePointer.EMPTY_ARRAY;
        }
        List<VirtualFilePointer> result = new ArrayList<>();
        for (ContentEntry contentEntry : moduleRootLayer.getContentEntries()) {
            VirtualFile root = contentEntry.getFile();
            if (root != null && root.findChild(NpmConstants.PACKAGE_JSON) != null) {
                result.add(new LightFilePointer(contentEntry.getUrl() + "/" + NpmConstants.NODE_MODULES));
            }
        }
        return result.isEmpty() ? VirtualFilePointer.EMPTY_ARRAY : result.toArray(VirtualFilePointer[]::new);
    }

    private static boolean hasNpmLibraries(ModuleRootLayer moduleRootLayer) {
        for (OrderEntry orderEntry : moduleRootLayer.getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry libraryOrderEntry) {
                String name = libraryOrderEntry.getLibraryName();
                if (name != null && name.startsWith(NpmConstants.LIBRARY_PREFIX)) {
                    return true;
                }
            }
        }
        return false;
    }
}
