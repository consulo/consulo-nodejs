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
import consulo.json.jom.JomFileElement;
import consulo.json.jom.JomManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.nodejs.impl.packages.jom.PackagesRootElement;
import consulo.nodejs.npm.NpmPackage;
import consulo.nodejs.npm.NpmPackageParser;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Reads a {@code package.json} through its JOM model ({@link PackagesRootElement}).
 *
 * @since 2026-10-31
 */
public final class NpmPackageReader {
    private NpmPackageReader() {
    }

    @RequiredReadAction
    public static @Nullable NpmPackage read(Project project, @Nullable VirtualFile packageJson) {
        if (packageJson == null || !packageJson.isValid() || packageJson.isDirectory()) {
            return null;
        }
        PsiFile file = PsiManager.getInstance(project).findFile(packageJson);
        if (file == null) {
            return null;
        }
        JomFileElement<PackagesRootElement> element = JomManager.getInstance(project).getFileElement(file);
        if (element == null) {
            return null;
        }
        PackagesRootElement root = element.getRootElement();
        return new NpmPackage(
            root.getName(),
            root.getVersion(),
            notNull(root.getDependencies()),
            notNull(root.getDevDependencies()),
            notNull(root.getPeerDependencies()),
            notNull(root.getOptionalDependencies()),
            notNull(root.getScripts()),
            workspaces(root, file)
        );
    }

    private static List<String> workspaces(PackagesRootElement root, PsiFile file) {
        List<String> workspaces;
        try {
            workspaces = root.getWorkspaces();
        }
        catch (RuntimeException e) {
            workspaces = null;
        }
        if (workspaces != null && !workspaces.isEmpty()) {
            return workspaces;
        }
        // the object form, not modelled by JOM
        NpmPackage npmPackage = NpmPackageParser.parse(file.getText());
        return npmPackage == null ? List.of() : npmPackage.workspaces();
    }

    private static Map<String, String> notNull(@Nullable Map<String, String> map) {
        return map == null ? Map.of() : map;
    }
}
