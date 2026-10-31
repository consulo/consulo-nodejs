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
package consulo.nodejs.impl.npm.setting;

import consulo.externalSystem.setting.ExternalProjectSettings;
import org.jspecify.annotations.Nullable;

/**
 * @since 2026-10-31
 */
public class NpmProjectSettings extends ExternalProjectSettings {
    private @Nullable String myNodeSdkName;

    /**
     * @return the name of the Node.js bundle running npm; {@code null} for the bundle of the module, or the first one
     */
    public @Nullable String getNodeSdkName() {
        return myNodeSdkName;
    }

    public void setNodeSdkName(@Nullable String nodeSdkName) {
        myNodeSdkName = nodeSdkName == null || nodeSdkName.isBlank() ? null : nodeSdkName;
    }

    @Override
    public NpmProjectSettings clone() {
        NpmProjectSettings clone = new NpmProjectSettings();
        copyTo(clone);
        clone.myNodeSdkName = myNodeSdkName;
        return clone;
    }
}
