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
package consulo.nodejs.run;

import consulo.disposer.Disposable;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.TextComponentAccessor;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A path field whose chosen files are stored relative to the directory of the configuration module when they are inside it.
 */
public final class NodeJSModuleRelativePathField {
    private NodeJSModuleRelativePathField() {
    }

    @RequiredUIAccess
    public static FileChooserTextBoxBuilder.Controller create(Project project,
                                                              Disposable parent,
                                                              LocalizeValue dialogTitle,
                                                              LocalizeValue dialogDescription,
                                                              FileChooserDescriptor descriptor,
                                                              Supplier<@Nullable Module> module) {
        return FileChooserTextBoxBuilder.create(project)
            .dialogTitle(dialogTitle)
            .dialogDescription(dialogDescription)
            .fileChooserDescriptor(descriptor)
            .textBoxAccessor(new TextComponentAccessor<>() {
                @RequiredUIAccess
                @Override
                public String getValue(TextBox textBox) {
                    return StringUtil.notNullize(textBox.getValue());
                }

                @RequiredUIAccess
                @Override
                public void setValue(TextBox textBox, String text, boolean fireListeners) {
                    Module selected = module.get();
                    String moduleDirPath = selected == null ? null : selected.getModuleDirPath();
                    String relativePath = moduleDirPath == null
                        ? null
                        : FileUtil.getRelativePath(moduleDirPath, FileUtil.toSystemIndependentName(text), '/');
                    textBox.setValue(StringUtil.isEmpty(relativePath) ? text : relativePath, fireListeners);
                }
            })
            .uiDisposable(parent)
            .build();
    }
}
