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
package consulo.nodejs.impl.run;

import com.dslplatform.json.CompiledJson;
import consulo.nodejs.run.NodeJSConfigurationBaseOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

@CompiledJson
public class NpmScriptConfigurationOptions extends NodeJSConfigurationBaseOptions {
    private @Nullable String myScript;

    @Attribute("script")
    public @Nullable String getScript() {
        return myScript;
    }

    public void setScript(@Nullable String script) {
        myScript = StringUtil.nullize(script, true);
    }
}
