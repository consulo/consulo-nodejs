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
package consulo.mocha.run;

import com.dslplatform.json.CompiledJson;
import consulo.nodejs.run.NodeJSConfigurationBaseOptions;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.annotation.Attribute;
import org.jspecify.annotations.Nullable;

/**
 * The tests to run; a {@code null} target type means {@link MochaConfiguration.TargetType#DIRECTORY}.
 */
@CompiledJson
public class MochaConfigurationOptions extends NodeJSConfigurationBaseOptions {
    private @Nullable String myTargetType;
    private @Nullable String myFilePath;
    private @Nullable String myDirectoryPath;

    @Attribute("targetType")
    public @Nullable String getTargetType() {
        return myTargetType;
    }

    public void setTargetType(@Nullable String targetType) {
        myTargetType = StringUtil.nullize(targetType, true);
    }

    @Attribute("filePath")
    public @Nullable String getFilePath() {
        return myFilePath;
    }

    public void setFilePath(@Nullable String filePath) {
        myFilePath = StringUtil.nullize(filePath, true);
    }

    @Attribute("directoryPath")
    public @Nullable String getDirectoryPath() {
        return myDirectoryPath;
    }

    public void setDirectoryPath(@Nullable String directoryPath) {
        myDirectoryPath = StringUtil.nullize(directoryPath, true);
    }
}
