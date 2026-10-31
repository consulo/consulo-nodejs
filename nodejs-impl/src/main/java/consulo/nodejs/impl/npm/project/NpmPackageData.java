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
package consulo.nodejs.impl.npm.project;

import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import consulo.nodejs.impl.npm.NpmSystem;
import consulo.nodejs.npm.NpmPackage;
import org.jspecify.annotations.Nullable;

/**
 * The {@code package.json} of a module, applied by {@link NpmPackageDataService}.
 * <p>
 * A child of the module node for the modules of npm; a child of the project node for a package in a module owned by
 * another build system (Maven), which is only extended, never taken over.
 *
 * @since 2026-10-31
 */
public class NpmPackageData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    /**
     * Processed after the module and its content roots exist.
     */
    public static final Key<NpmPackageData> KEY = Key.create(NpmPackageData.class, ProjectKeys.CONTENT_ROOT.getProcessingWeight() + 1);

    private final NpmPackage myPackage;
    private final String myPackageDirectory;

    public NpmPackageData(NpmPackage npmPackage, String packageDirectory) {
        super(NpmSystem.SYSTEM_ID);
        myPackage = npmPackage;
        myPackageDirectory = packageDirectory;
    }

    public NpmPackage getPackage() {
        return myPackage;
    }

    /**
     * @return the directory of the {@code package.json}
     */
    public String getPackageDirectory() {
        return myPackageDirectory;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof NpmPackageData that) || !super.equals(o)) {
            return false;
        }
        return myPackage.equals(that.myPackage) && myPackageDirectory.equals(that.myPackageDirectory);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * super.hashCode() + myPackage.hashCode()) + myPackageDirectory.hashCode();
    }
}
