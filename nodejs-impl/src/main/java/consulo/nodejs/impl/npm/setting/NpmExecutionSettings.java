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

import consulo.externalSystem.model.setting.ExternalSystemExecutionSettings;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * @since 2026-10-31
 */
public class NpmExecutionSettings extends ExternalSystemExecutionSettings {
    private final String myNpmExecutable;
    private final @Nullable String myNodeDirectory;

    public NpmExecutionSettings(String npmExecutable, @Nullable String nodeDirectory) {
        myNpmExecutable = npmExecutable;
        myNodeDirectory = nodeDirectory;
    }

    /**
     * @return the {@code npm} of the Node.js bundle, or {@code npm} looked up in {@code PATH}
     */
    public String getNpmExecutable() {
        return myNpmExecutable;
    }

    /**
     * @return the directory of the {@code node} executable of the bundle, put first in {@code PATH}: npm and the
     * scripts start {@code node} through {@code #!/usr/bin/env node}
     */
    public @Nullable String getNodeDirectory() {
        return myNodeDirectory;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + myNpmExecutable.hashCode();
        result = 31 * result + Objects.hashCode(myNodeDirectory);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof NpmExecutionSettings that) || !super.equals(o)) {
            return false;
        }
        return myNpmExecutable.equals(that.myNpmExecutable) && Objects.equals(myNodeDirectory, that.myNodeDirectory);
    }
}
