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
package consulo.nodejs.impl.newProjectOrModule;

import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.nodejs.newProjectOrModule.NodeJSNewModuleSetupStep;
import consulo.ui.RadioGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;

import java.util.List;

/**
 * The name, the Node.js bundle and the language of a Vite application.
 *
 * @since 2026-10-31
 */
public class ViteNewModuleSetupStep extends NodeJSNewModuleSetupStep<ViteNewModuleContext> {
    private final ViteNewModuleContext myContext;

    public ViteNewModuleSetupStep(ViteNewModuleContext context) {
        super(context);
        myContext = context;
    }

    @RequiredUIAccess
    @Override
    protected void extend(FormBuilder builder, Disposable uiDisposable) {
        super.extend(builder, uiDisposable);

        ViteNewModuleContext context = myContext;
        RadioGroup<ViteTemplate> templates = RadioGroup.create();
        builder.addLabeled(
            LocalizeValue.localizeTODO("Language:"),
            templates.fillHorizontal(List.of(ViteTemplate.values()), ViteTemplate::getPresentableName)
        );
        templates.setValue(context.getTemplate());
        templates.addValueListener(template -> {
            if (template != null) {
                context.setTemplate(template);
            }
        });
    }
}
