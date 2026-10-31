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

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemMutableModuleExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;
import consulo.ui.image.ImageEffects;
import org.jspecify.annotations.Nullable;

/**
 * Another build system owning modules, like Maven: the npm import only extends its modules.
 */
@ExtensionImpl
public class TestBuildSystemModuleExtensionProvider implements ModuleExtensionProvider<ExternalSystemModuleExtensionImpl> {
    public static final ProjectSystemId SYSTEM_ID = new ProjectSystemId("TEST-BUILD", LocalizeValue.of("Test build"), ImageEffects.empty(16));

    @Override
    public String getId() {
        return SYSTEM_ID.getId();
    }

    @Override
    public @Nullable String getParentId() {
        return null;
    }

    @Override
    public boolean isSystemOnly() {
        return true;
    }

    @Override
    public LocalizeValue getName() {
        return LocalizeValue.of("Test build");
    }

    @Override
    public Image getIcon() {
        return ImageEffects.empty(16);
    }

    @Override
    public ModuleExtension<ExternalSystemModuleExtensionImpl> createImmutableExtension(ModuleRootLayer moduleRootLayer) {
        return new ExternalSystemModuleExtensionImpl(getId(), moduleRootLayer, SYSTEM_ID);
    }

    @Override
    public MutableModuleExtension<ExternalSystemModuleExtensionImpl> createMutableExtension(ModuleRootLayer moduleRootLayer) {
        return new ExternalSystemMutableModuleExtensionImpl(getId(), moduleRootLayer, SYSTEM_ID);
    }
}
