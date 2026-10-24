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

import com.dslplatform.json.CompiledJson;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

/**
 * Settings shared by the Node.js based configurations; program parameters, working directory and environment are fragment states.
 */
@CompiledJson
public class NodeJSConfigurationBaseOptions extends RunConfigurationOptions {
    private @Nullable String myVmParameters;
    private boolean myUseAlternativeBundle;
    private @Nullable String myAlternativeBundleName;

    @Attribute("vmParameters")
    public @Nullable String getVmParameters() {
        return myVmParameters;
    }

    public void setVmParameters(@Nullable String vmParameters) {
        myVmParameters = StringUtil.nullize(vmParameters, true);
    }

    @Attribute("useAlternativeBundle")
    public boolean isUseAlternativeBundle() {
        return myUseAlternativeBundle;
    }

    public void setUseAlternativeBundle(boolean useAlternativeBundle) {
        myUseAlternativeBundle = useAlternativeBundle;
    }

    @Attribute("alternativeBundleName")
    public @Nullable String getAlternativeBundleName() {
        return myAlternativeBundleName;
    }

    public void setAlternativeBundleName(@Nullable String alternativeBundleName) {
        myAlternativeBundleName = StringUtil.nullize(alternativeBundleName, true);
    }
}
