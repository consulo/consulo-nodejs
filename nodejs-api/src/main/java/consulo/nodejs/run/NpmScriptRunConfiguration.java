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

import org.jspecify.annotations.Nullable;

/**
 * A run configuration running a script of {@code package.json}: {@code npm run <script>}.
 *
 * @since 2026-10-31
 */
public interface NpmScriptRunConfiguration {
    /**
     * The id of its configuration type.
     */
    String TYPE_ID = "NpmScriptConfigurationType";

    @Nullable
    String getScript();

    void setScript(@Nullable String script);
}
