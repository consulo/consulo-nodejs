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
package consulo.nodejs.it;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.module.Module;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.nodejs.npm.NpmImporter;
import consulo.nodejs.npm.NpmPackage;
import consulo.virtualFileSystem.VirtualFile;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Records where it is applied: a framework of the npm package {@link #FRAMEWORK}.
 */
@ExtensionImpl
public class TestNpmImporter implements NpmImporter {
    public static final String FRAMEWORK = "acme-framework";

    /**
     * {@code <module>: <package name> @ <package root>}
     */
    public static final List<String> PROCESSED = new CopyOnWriteArrayList<>();

    @Override
    public boolean isApplicable(NpmPackage npmPackage) {
        return npmPackage.requires(FRAMEWORK);
    }

    @RequiredReadAction
    @Override
    public void process(Module module, ModifiableRootModel model, NpmPackage npmPackage, VirtualFile packageRoot) {
        PROCESSED.add(module.getName() + ": " + npmPackage.name() + " @ " + packageRoot.getName());
    }

    @Override
    public void collectExcludedDirectories(NpmPackage npmPackage, Consumer<String> relativePaths) {
        relativePaths.accept(".acme");
    }
}
