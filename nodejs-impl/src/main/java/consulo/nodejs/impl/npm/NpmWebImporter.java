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
import consulo.nodejs.npm.NpmConstants;
import consulo.nodejs.npm.NpmImporterFromDependency;

/**
 * A frontend (Vite, React, Svelte, ...) runs in a browser: the Web extension, with the standard library of the DOM.
 *
 * @since 2026-10-31
 */
@ExtensionImpl
public class NpmWebImporter extends NpmImporterFromDependency {
    public NpmWebImporter() {
        super(NpmConstants.WEB_EXTENSION_ID, NpmConstants.CLIENT_PACKAGES.toArray(String[]::new));
    }
}
