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
import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.externalSystem.setting.DelegatingExternalSystemSettingsListener;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;
import consulo.project.Project;
import consulo.util.xml.serializer.annotation.AbstractCollection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

import java.util.Set;
import java.util.TreeSet;

/**
 * The linked npm packages of the project.
 *
 * @since 2026-10-31
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "NpmSettings", storages = @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/npm.xml"))
public class NpmSettings extends AbstractExternalSystemSettings<NpmSettings, NpmProjectSettings, NpmSettingsListener>
    implements PersistentStateComponent<NpmSettings.MyState> {
    @Inject
    public NpmSettings(Project project) {
        super(NpmSettingsListener.TOPIC, project);
    }

    public static NpmSettings getInstance(Project project) {
        return project.getInstance(NpmSettings.class);
    }

    @Override
    public void subscribe(ExternalSystemSettingsListener<NpmProjectSettings> listener) {
        getProject().getMessageBus().connect(getProject()).subscribe(NpmSettingsListener.TOPIC, new ListenerAdapter(listener));
    }

    @Override
    protected void copyExtraSettingsFrom(NpmSettings settings) {
    }

    @Override
    protected void checkSettings(NpmProjectSettings old, NpmProjectSettings current) {
    }

    @Override
    public MyState getState() {
        MyState state = new MyState();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(MyState state) {
        super.loadState(state);
    }

    private static class ListenerAdapter extends DelegatingExternalSystemSettingsListener<NpmProjectSettings>
        implements NpmSettingsListener {
        ListenerAdapter(ExternalSystemSettingsListener<NpmProjectSettings> delegate) {
            super(delegate);
        }
    }

    public static class MyState implements State<NpmProjectSettings> {
        private final Set<NpmProjectSettings> myProjectSettings = new TreeSet<>();

        @Override
        @AbstractCollection(surroundWithTag = false, elementTypes = {NpmProjectSettings.class})
        public Set<NpmProjectSettings> getLinkedExternalProjectsSettings() {
            return myProjectSettings;
        }

        @Override
        public void setLinkedExternalProjectsSettings(@Nullable Set<NpmProjectSettings> settings) {
            if (settings != null) {
                myProjectSettings.addAll(settings);
            }
        }
    }
}
