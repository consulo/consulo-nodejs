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

import consulo.localize.LocalizeValue;

/**
 * The templates of {@code create-vite} offered by the new project wizard.
 *
 * @since 2026-10-31
 */
public enum ViteTemplate {
    JAVASCRIPT("vanilla", LocalizeValue.localizeTODO("JavaScript")),
    TYPESCRIPT("vanilla-ts", LocalizeValue.localizeTODO("TypeScript"));

    private final String myTemplate;
    private final LocalizeValue myPresentableName;

    ViteTemplate(String template, LocalizeValue presentableName) {
        myTemplate = template;
        myPresentableName = presentableName;
    }

    /**
     * @return the {@code --template} of {@code create-vite}
     */
    public String getTemplate() {
        return myTemplate;
    }

    public LocalizeValue getPresentableName() {
        return myPresentableName;
    }
}
