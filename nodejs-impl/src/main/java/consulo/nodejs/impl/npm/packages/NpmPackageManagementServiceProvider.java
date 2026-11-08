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
package consulo.nodejs.impl.npm.packages;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.nodejs.npm.NpmProjectModel;
import consulo.project.Project;
import consulo.repository.ui.PackageManagementService;
import consulo.repository.ui.PackageManagementServiceProvider;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

/**
 * The npm dependencies of a module - the target is the name of the module, the dependencies are the ones of its package.json.
 */
@ExtensionImpl
public class NpmPackageManagementServiceProvider implements PackageManagementServiceProvider {
    @Override
    public String getId() {
        return "npm";
    }

    @Override
    public LocalizeValue getName() {
        return LocalizeValue.localizeTODO("npm");
    }

    @Override
    public Image getIcon() {
        return NodeJSApiIconGroup.npm();
    }

    @Override
    public @Nullable PackageManagementService create(Project project, String target) {
        return ReadAction.compute(() -> {
            Module module = ModuleManager.getInstance(project).findModuleByName(target);
            VirtualFile packageJson = module == null ? null : NpmProjectModel.getInstance(project).findPackageJson(module);
            return packageJson == null ? null : new NpmPackageManagementService(module, packageJson);
        });
    }
}
