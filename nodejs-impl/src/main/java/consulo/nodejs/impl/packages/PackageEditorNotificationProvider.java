/*
 * Copyright 2013-2017 consulo.io
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

package consulo.nodejs.impl.packages;

import com.intellij.json.JsonFileType;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.EditorNotificationBuilder;
import consulo.fileEditor.EditorNotificationProvider;
import consulo.fileEditor.FileEditor;
import consulo.json.jom.JomElement;
import consulo.json.jom.JomFileElement;
import consulo.json.jom.JomManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.localize.LocalizeValue;
import consulo.nodejs.packages.call.NpmRunUtil;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 05.12.2015
 */
@ExtensionImpl
public class PackageEditorNotificationProvider implements EditorNotificationProvider {
    private final Project myProject;

    @Inject
    public PackageEditorNotificationProvider(Project project) {
        myProject = project;
    }

    @Nonnull
    @Override
    public String getId() {
        return "npm";
    }

    @RequiredReadAction
    @Nullable
    @Override
    public EditorNotificationBuilder buildNotification(@Nonnull VirtualFile file, @Nonnull FileEditor fileEditor, @Nonnull Supplier<EditorNotificationBuilder> supplier) {
        if (file.getFileType() != JsonFileType.INSTANCE) {
            return null;
        }

        final PsiFile jsonFile = PsiManager.getInstance(myProject).findFile(file);
        if (jsonFile == null) {
            return null;
        }
        JomFileElement<JomElement> fileElement = JomManager.getInstance(myProject).getFileElement(jsonFile);
        if (fileElement == null) {
            return null;
        }
        EditorNotificationBuilder builder = supplier.get();
        builder.withText(LocalizeValue.localizeTODO("npm"));
        builder.withAction(LocalizeValue.localizeTODO("Update"), e -> NpmRunUtil.run(myProject, file, NpmRunUtil.UPDATE));
        return builder;
    }
}
