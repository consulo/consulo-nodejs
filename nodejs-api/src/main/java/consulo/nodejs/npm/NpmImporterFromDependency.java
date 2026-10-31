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
import consulo.module.Module;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.nodejs.packages.detect.NpmModuleExtensions;
import consulo.virtualFileSystem.VirtualFile;

import java.util.List;

/**
 * Enables a module extension (and its parents) when the package depends on one of the npm packages of a framework, like
 * the {@code MavenImporterFromDependency}.
 *
 * @since 2026-10-31
 */
public abstract class NpmImporterFromDependency implements NpmImporter {
    private final String myExtensionId;
    private final List<String> myPackageNames;

    /**
     * @param extensionId  the id of the module extension
     * @param packageNames the npm packages which mean the framework; a name ending with {@code /} is a scope prefix
     *                     ({@code @vaadin/})
     */
    protected NpmImporterFromDependency(String extensionId, String... packageNames) {
        myExtensionId = extensionId;
        myPackageNames = List.of(packageNames);
    }

    public String getExtensionId() {
        return myExtensionId;
    }

    @Override
    public boolean isApplicable(NpmPackage npmPackage) {
        for (String packageName : myPackageNames) {
            if (packageName.endsWith("/") ? npmPackage.requiresAnyWithPrefix(packageName) : npmPackage.requires(packageName)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    @Override
    public void process(Module module, ModifiableRootModel model, NpmPackage npmPackage, VirtualFile packageRoot) {
        NpmModuleExtensions.enableWithParents(model, myExtensionId);
    }
}
