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

import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.nodejs.npm.NpmConstants;
import consulo.nodejs.impl.npm.setting.NpmProjectSettings;
import consulo.project.Project;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A linked package is imported again when its {@code package.json} or lock file changes.
 *
 * @since 2026-10-31
 */
public class NpmAutoImportAware implements ExternalSystemAutoImportAware {
    @Override
    public @Nullable String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        Path changed = Path.of(changedFileOrDirPath);
        Path fileName = changed.getFileName();
        if (fileName == null || !NpmConstants.AUTO_IMPORT_FILES.contains(fileName.toString())) {
            return null;
        }
        if (changedFileOrDirPath.contains("/" + NpmConstants.NODE_MODULES + "/")) {
            return null;
        }
        // the package.json of a workspace imports its root again
        for (Path directory = changed.getParent(); directory != null; directory = directory.getParent()) {
            NpmProjectSettings settings = NpmUtil.findProjectSettings(project, directory.toString());
            if (settings != null) {
                return settings.getExternalProjectPath();
            }
        }
        return null;
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        Path directory = NpmUtil.toPackageDirectory(Path.of(projectPath));
        List<Path> result = new ArrayList<>();
        for (String file : NpmConstants.AUTO_IMPORT_FILES) {
            result.add(directory.resolve(file));
        }
        return result;
    }
}
