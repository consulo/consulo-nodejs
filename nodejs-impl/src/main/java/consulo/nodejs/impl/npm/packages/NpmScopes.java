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
package consulo.nodejs.impl.npm.packages;

import consulo.localize.LocalizeValue;
import consulo.repository.ui.PackageInstallScope;

import java.util.List;

/**
 * The sections of the dependencies of a package.json.
 */
public final class NpmScopes {
    public static final String PROD = "dependencies";
    public static final String DEV = "devDependencies";
    public static final String PEER = "peerDependencies";
    public static final String OPTIONAL = "optionalDependencies";

    public static final PackageInstallScope PROD_SCOPE = new PackageInstallScope(PROD, LocalizeValue.localizeTODO("dependencies"));
    public static final PackageInstallScope DEV_SCOPE = new PackageInstallScope(DEV, LocalizeValue.localizeTODO("devDependencies"));
    public static final PackageInstallScope PEER_SCOPE = new PackageInstallScope(PEER, LocalizeValue.localizeTODO("peerDependencies"));
    public static final PackageInstallScope OPTIONAL_SCOPE = new PackageInstallScope(OPTIONAL, LocalizeValue.localizeTODO("optionalDependencies"));

    public static final List<PackageInstallScope> ALL = List.of(PROD_SCOPE, DEV_SCOPE, PEER_SCOPE, OPTIONAL_SCOPE);

    public static PackageInstallScope byId(String id) {
        for (PackageInstallScope scope : ALL) {
            if (scope.id().equals(id)) {
                return scope;
            }
        }
        return PROD_SCOPE;
    }

    private NpmScopes() {
    }
}
