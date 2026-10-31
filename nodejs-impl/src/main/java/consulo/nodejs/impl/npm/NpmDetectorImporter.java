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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.module.Module;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.nodejs.npm.NpmImporter;
import consulo.nodejs.npm.NpmPackage;
import consulo.nodejs.packages.detect.NpmModuleExtensionDetector;
import consulo.nodejs.packages.detect.NpmModuleExtensions;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

/**
 * The frameworks of the {@link NpmModuleExtensionDetector}s (Vue.js, Angular) as an {@link NpmImporter}: their extensions
 * are enabled on the import of the package too, not only through the editor notification.
 *
 * @since 2026-10-31
 */
@ExtensionImpl
public class NpmDetectorImporter implements NpmImporter {
    private final Application myApplication;

    @Inject
    public NpmDetectorImporter(Application application) {
        myApplication = application;
    }

    @Override
    public boolean isApplicable(NpmPackage npmPackage) {
        for (NpmModuleExtensionDetector detector : myApplication.getExtensionList(NpmModuleExtensionDetector.class)) {
            if (isDetected(detector, npmPackage)) {
                return true;
            }
        }
        return false;
    }

    @RequiredReadAction
    @Override
    public void process(Module module, ModifiableRootModel model, NpmPackage npmPackage, VirtualFile packageRoot) {
        for (NpmModuleExtensionDetector detector : myApplication.getExtensionList(NpmModuleExtensionDetector.class)) {
            if (isDetected(detector, npmPackage)) {
                NpmModuleExtensions.enableWithParents(model, detector.getExtensionId());
            }
        }
    }

    private static boolean isDetected(NpmModuleExtensionDetector detector, NpmPackage npmPackage) {
        for (String packageName : detector.getPackageNames()) {
            if (npmPackage.requires(packageName)) {
                return true;
            }
        }
        return false;
    }
}
