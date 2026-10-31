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
package consulo.nodejs.impl.npm.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.externalSystem.setting.AbstractExternalSystemLocalSettings;
import consulo.nodejs.impl.npm.NpmSystem;
import consulo.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @since 2026-10-31
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "NpmLocalSettings", storages = @Storage(file = StoragePathMacros.WORKSPACE_FILE))
public class NpmLocalSettings extends AbstractExternalSystemLocalSettings
    implements PersistentStateComponent<AbstractExternalSystemLocalSettings.State> {
    @Inject
    public NpmLocalSettings(Project project) {
        super(NpmSystem.SYSTEM_ID, project);
    }

    public static NpmLocalSettings getInstance(Project project) {
        return project.getInstance(NpmLocalSettings.class);
    }

    @Override
    public State getState() {
        State state = new State();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(State state) {
        super.loadState(state);
    }
}
