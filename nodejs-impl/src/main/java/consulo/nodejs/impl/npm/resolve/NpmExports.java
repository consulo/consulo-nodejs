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
package consulo.nodejs.impl.npm.resolve;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The {@code exports} of a package.json (Node.js "package entry points"): a subpath ({@code .}, {@code ./utils},
 * patterns {@code ./*}) to a target, with conditions ({@code import}, {@code require}, {@code types}, ...).
 *
 * @since 2026-10-31
 */
final class NpmExports {
    private NpmExports() {
    }

    /**
     * @param subpath    {@code .} or {@code ./x}
     * @param conditions the conditions of the import, in priority
     * @return the target relative to the package ({@code ./dist/index.mjs}), {@code null} if not exported
     */
    static @Nullable String resolve(Object exports, String subpath, List<String> conditions) {
        if (!(exports instanceof Map<?, ?> map) || !isSubpathMap(map)) {
            // "exports": "./index.js" or the conditions of "."
            return ".".equals(subpath) ? target(exports, conditions, null) : null;
        }
        Object exact = map.get(subpath);
        if (exact != null) {
            return target(exact, conditions, null);
        }
        // the longest pattern prefix wins: "./*", "./features/*.js"
        String bestKey = null;
        String bestMatch = null;
        for (Object key : map.keySet()) {
            String pattern = String.valueOf(key);
            int star = pattern.indexOf('*');
            if (star < 0) {
                continue;
            }
            String prefix = pattern.substring(0, star);
            String suffix = pattern.substring(star + 1);
            if (subpath.startsWith(prefix) && subpath.endsWith(suffix) && subpath.length() >= prefix.length() + suffix.length()
                && (bestKey == null || prefix.length() > bestKey.indexOf('*'))) {
                bestKey = pattern;
                bestMatch = subpath.substring(prefix.length(), subpath.length() - suffix.length());
            }
        }
        if (bestKey == null) {
            // the legacy folder mapping "./": "./"
            for (Object key : map.keySet()) {
                String folder = String.valueOf(key);
                if (folder.endsWith("/") && subpath.startsWith(folder) && map.get(key) instanceof String target) {
                    return target + subpath.substring(folder.length());
                }
            }
            return null;
        }
        return target(map.get(bestKey), conditions, bestMatch);
    }

    private static boolean isSubpathMap(Map<?, ?> map) {
        for (Object key : map.keySet()) {
            if (String.valueOf(key).startsWith(".")) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param match the part matched by the {@code *} of a pattern, put in the {@code *} of the target
     */
    private static @Nullable String target(@Nullable Object value, List<String> conditions, @Nullable String match) {
        if (value instanceof String string) {
            return match == null ? string : string.replace("*", match);
        }
        if (value instanceof List<?> alternatives) {
            for (Object alternative : alternatives) {
                String target = target(alternative, conditions, match);
                if (target != null) {
                    return target;
                }
            }
            return null;
        }
        if (value instanceof Map<?, ?> conditional) {
            // the conditions are matched in the order of the package.json
            for (Map.Entry<?, ?> entry : conditional.entrySet()) {
                if (conditions.contains(String.valueOf(entry.getKey()))) {
                    String target = target(entry.getValue(), conditions, match);
                    if (target != null) {
                        return target;
                    }
                }
            }
        }
        return null;
    }
}
