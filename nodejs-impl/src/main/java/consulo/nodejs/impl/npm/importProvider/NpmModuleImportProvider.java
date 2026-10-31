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
import consulo.externalSystem.importing.AbstractExternalModuleImportProvider;
import consulo.externalSystem.importing.ExternalModuleImportContext;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.nodejs.impl.npm.NpmSystem;
import consulo.nodejs.npm.NpmConstants;
import consulo.project.Project;
import consulo.ui.image.Image;
import jakarta.inject.Inject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Imports a directory with a {@code package.json} as a linked npm package.
 *
 * @since 2026-10-31
 */
@ExtensionImpl
public class NpmModuleImportProvider extends AbstractExternalModuleImportProvider {
    @Inject
    public NpmModuleImportProvider(ProjectDataManager dataManager) {
        super(dataManager, NpmSystem.SYSTEM_ID);
    }

    @Override
    public Image getIcon() {
        return NodeJSApiIconGroup.npm();
    }

    @Override
    public boolean canImport(Path fileOrDirectory) {
        if (Files.isDirectory(fileOrDirectory)) {
            return Files.exists(fileOrDirectory.resolve(NpmConstants.PACKAGE_JSON));
        }
        Path fileName = fileOrDirectory.getFileName();
        return fileName != null && NpmConstants.PACKAGE_JSON.equals(fileName.toString());
    }

    @Override
    public boolean isOnlyForNewImport() {
        // the "link project" action of the npm tool window imports into the open project
        return false;
    }

    @Override
    protected File getExternalProjectConfigToUse(File file) {
        return file.isDirectory() ? file : file.getParentFile();
    }

    @Override
    protected void doPrepare(ExternalModuleImportContext context) {
    }

    @Override
    protected void beforeCommit(DataNode<ProjectData> dataNode, Project project) {
    }

    @Override
    protected void applyExtraSettings(ExternalModuleImportContext context) {
    }
}
