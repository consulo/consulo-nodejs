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
import consulo.externalSystem.ExternalSystemManager;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware;
import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.nodejs.impl.npm.project.NpmProjectResolver;
import consulo.nodejs.impl.npm.setting.NpmExecutionSettings;
import consulo.nodejs.impl.npm.setting.NpmLocalSettings;
import consulo.nodejs.impl.npm.setting.NpmProjectSettings;
import consulo.nodejs.impl.npm.setting.NpmSettings;
import consulo.nodejs.impl.npm.setting.NpmSettingsListener;
import consulo.nodejs.impl.npm.task.NpmTaskManager;
import consulo.nodejs.npm.NpmConstants;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @since 2026-10-31
 */
@ExtensionImpl
public class NpmManager implements ExternalSystemAutoImportAware,
    ExternalSystemManager<NpmProjectSettings, NpmSettingsListener, NpmSettings, NpmLocalSettings, NpmExecutionSettings> {

    private final NpmAutoImportAware myAutoImportAware = new NpmAutoImportAware();
    private final ExternalSystemAutoImportAware myAutoImportDelegate = new CachingExternalSystemAutoImportAware(myAutoImportAware);

    @Override
    public ProjectSystemId getSystemId() {
        return NpmSystem.SYSTEM_ID;
    }

    @Override
    public Function<Project, NpmSettings> getSettingsProvider() {
        return NpmSettings::getInstance;
    }

    @Override
    public Function<Project, NpmLocalSettings> getLocalSettingsProvider() {
        return NpmLocalSettings::getInstance;
    }

    @Override
    public Function<Pair<Project, String>, NpmExecutionSettings> getExecutionSettingsProvider() {
        return pair -> NpmUtil.createExecutionSettings(pair.getFirst(), pair.getSecond());
    }

    @Override
    public Supplier<? extends ExternalSystemProjectResolver<NpmExecutionSettings>> getProjectResolverFactory() {
        return NpmProjectResolver::new;
    }

    @Override
    public Supplier<? extends ExternalSystemTaskManager<NpmExecutionSettings>> getTaskManagerFactory() {
        return NpmTaskManager::new;
    }

    @Override
    public FileChooserDescriptor getExternalProjectDescriptor() {
        return new FileChooserDescriptor(true, true, false, false, false, false) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return file.isDirectory() || NpmConstants.PACKAGE_JSON.equals(file.getName());
            }
        };
    }

    @Override
    public @Nullable String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        return myAutoImportDelegate.getAffectedExternalProjectPath(changedFileOrDirPath, project);
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        return myAutoImportAware.getAffectedExternalProjectFilePaths(projectPath, project);
    }
}
