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
package consulo.nodejs.impl.npm;

import consulo.nodejs.npm.NpmConstants;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * The packages of the {@code workspaces} of a monorepo, like npm finds them.
 *
 * @since 2026-10-31
 */
public final class NpmWorkspaces {
    private static final int MAX_DEPTH = 8;

    private NpmWorkspaces() {
    }

    /**
     * @param patterns globs relative to the root ({@code packages/*}, {@code apps/**}); a {@code !} pattern excludes
     * @return the directories with a {@code package.json} matched by the patterns, sorted, without the root
     */
    public static List<Path> findPackageDirectories(Path root, List<String> patterns) {
        List<PathMatcher> includes = new ArrayList<>();
        List<PathMatcher> excludes = new ArrayList<>();
        for (String pattern : patterns) {
            boolean exclude = pattern.startsWith("!");
            String glob = trim(exclude ? pattern.substring(1) : pattern);
            if (glob.isEmpty()) {
                continue;
            }
            PathMatcher matcher;
            try {
                matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
            }
            catch (IllegalArgumentException e) {
                continue;
            }
            (exclude ? excludes : includes).add(matcher);
        }
        if (includes.isEmpty() || !Files.isDirectory(root)) {
            return List.of();
        }

        Set<Path> result = new TreeSet<>();
        try {
            Files.walkFileTree(root, EnumSet.noneOf(FileVisitOption.class), MAX_DEPTH, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attributes) {
                    if (directory.equals(root)) {
                        return FileVisitResult.CONTINUE;
                    }
                    Path name = directory.getFileName();
                    if (name == null || NpmConstants.NODE_MODULES.equals(name.toString()) || name.toString().startsWith(".")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    Path relative = root.relativize(directory);
                    if (matches(includes, relative) && !matches(excludes, relative)
                        && Files.isRegularFile(directory.resolve(NpmConstants.PACKAGE_JSON))) {
                        result.add(directory);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e) {
            return List.copyOf(result);
        }
        return List.copyOf(result);
    }

    private static boolean matches(List<PathMatcher> matchers, Path relative) {
        for (PathMatcher matcher : matchers) {
            if (matcher.matches(relative)) {
                return true;
            }
        }
        return false;
    }

    private static String trim(String pattern) {
        String result = pattern.trim();
        while (result.startsWith("./")) {
            result = result.substring(2);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
