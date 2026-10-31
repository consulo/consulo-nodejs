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
package consulo.nodejs.impl.npm.importProvider;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsConfigurableFactory;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.nodejs.impl.npm.NpmSystem;
import consulo.nodejs.impl.npm.setting.NpmProjectSettings;
import consulo.nodejs.impl.npm.setting.NpmSettings;
import consulo.project.ProjectManager;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * @since 2026-10-31
 */
@ExtensionImpl
public class NpmSettingsConfigurableFactory implements ExternalSystemSettingsConfigurableFactory<NpmProjectSettings, NpmSettings> {
    @Override
    public ProjectSystemId getSystemId() {
        return NpmSystem.SYSTEM_ID;
    }

    @Override
    public NpmProjectSettings createProjectSettings() {
        return new NpmProjectSettings();
    }

    @Override
    public NpmSettings createSystemSettings() {
        return new NpmSettings(ProjectManager.getInstance().getDefaultProject());
    }

    @Override
    @RequiredUIAccess
    public AbstractExternalProjectSettingsConfigurable<NpmProjectSettings> createProjectSettingsConfigurable(
        NpmProjectSettings settings,
        ExternalSystemSettingsPlace place
    ) {
        return new NpmProjectSettingsConfigurable(settings, place);
    }
}
