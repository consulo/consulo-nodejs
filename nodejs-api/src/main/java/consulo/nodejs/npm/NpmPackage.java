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
package consulo.nodejs.npm;

import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The model of a {@code package.json} ({@link NpmPackageParser}).
 *
 * @param dependencies         {@code dependencies}: name to version range
 * @param devDependencies      {@code devDependencies}
 * @param peerDependencies     {@code peerDependencies}
 * @param optionalDependencies {@code optionalDependencies}
 * @param scripts              {@code scripts}: name to command
 * @param workspaces           {@code workspaces}: the patterns of the directories of the packages of a monorepo
 *                             ({@code packages/*}), relative to the package directory
 * @since 2026-10-31
 */
public record NpmPackage(@Nullable String name,
                         @Nullable String version,
                         Map<String, String> dependencies,
                         Map<String, String> devDependencies,
                         Map<String, String> peerDependencies,
                         Map<String, String> optionalDependencies,
                         Map<String, String> scripts,
                         List<String> workspaces) implements Serializable {
    public static final NpmPackage EMPTY = new NpmPackage(null, null, Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), List.of());

    public NpmPackage {
        dependencies = copy(dependencies);
        devDependencies = copy(devDependencies);
        peerDependencies = copy(peerDependencies);
        optionalDependencies = copy(optionalDependencies);
        scripts = copy(scripts);
        workspaces = List.copyOf(workspaces);
    }

    public NpmPackage(@Nullable String name,
                      @Nullable String version,
                      Map<String, String> dependencies,
                      Map<String, String> devDependencies,
                      Map<String, String> peerDependencies,
                      Map<String, String> optionalDependencies,
                      Map<String, String> scripts) {
        this(name, version, dependencies, devDependencies, peerDependencies, optionalDependencies, scripts, List.of());
    }

    /**
     * @return whether the package depends on {@code packageName} (also as dev, peer or optional dependency)
     */
    public boolean requires(String packageName) {
        return dependencies.containsKey(packageName)
            || devDependencies.containsKey(packageName)
            || peerDependencies.containsKey(packageName)
            || optionalDependencies.containsKey(packageName);
    }

    /**
     * @param prefix like {@code @angular/}
     */
    public boolean requiresAnyWithPrefix(String prefix) {
        for (String dependency : getAllDependencyNames()) {
            if (dependency.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the version range of a dependency, {@code null} if the package doesn't depend on it
     */
    public @Nullable String version(String packageName) {
        String version = dependencies.get(packageName);
        if (version == null) {
            version = devDependencies.get(packageName);
        }
        if (version == null) {
            version = peerDependencies.get(packageName);
        }
        if (version == null) {
            version = optionalDependencies.get(packageName);
        }
        return version;
    }

    /**
     * @return whether the dependency is needed only for development (tests, build): a dev dependency which is not a
     * dependency too
     */
    public boolean isDevOnly(String packageName) {
        return devDependencies.containsKey(packageName) && !dependencies.containsKey(packageName)
            && !optionalDependencies.containsKey(packageName) && !peerDependencies.containsKey(packageName);
    }

    /**
     * The names of all the dependencies: the dependencies, then the dev, peer and optional ones
     */
    public Set<String> getAllDependencyNames() {
        Set<String> result = new LinkedHashSet<>(dependencies.keySet());
        result.addAll(devDependencies.keySet());
        result.addAll(peerDependencies.keySet());
        result.addAll(optionalDependencies.keySet());
        return result;
    }

    private static Map<String, String> copy(Map<String, String> map) {
        return map.isEmpty() ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }
}
