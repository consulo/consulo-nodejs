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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.javascript.module.resolve.JavaScriptModuleRequest;
import consulo.javascript.module.resolve.JavaScriptModuleResolver;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.nodejs.npm.NpmConstants;
import consulo.nodejs.npm.NpmPackageParser;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The packages of npm, like Node.js and TypeScript find them:
 * <ul>
 * <li>the package in the {@code node_modules} of the importing directory or of a directory above, else a package of
 * the project (a workspace of a monorepo, not installed yet), else its declarations in {@code @types};</li>
 * <li>in the package: the {@code exports} of its package.json with the conditions of the import, else
 * {@code types}/{@code typings} (TypeScript), {@code module} (an import), {@code main}, else the {@code index} file;</li>
 * <li>a subpath ({@code vue/dist/vue.esm.js}) through the {@code exports}, else as a file of the package.</li>
 * </ul>
 *
 * @since 2026-10-31
 */
@ExtensionImpl
public class NpmModuleResolver implements JavaScriptModuleResolver {
    private static final List<String> SCRIPT_EXTENSIONS = List.of(".js", ".mjs", ".cjs", ".jsx", ".ts", ".tsx", ".d.ts", ".json");
    private static final List<String> TYPES_EXTENSIONS = List.of(".d.ts", ".ts", ".tsx", ".d.mts", ".d.cts", ".js", ".mjs", ".cjs", ".json");

    /**
     * The parsed package.json by file, with the modification stamp they were read at
     */
    private final ConcurrentMap<VirtualFile, Manifest> myManifests = new ConcurrentHashMap<>();

    @RequiredReadAction
    @Override
    public @Nullable VirtualFile resolve(JavaScriptModuleRequest request) {
        String specifier = request.specifier();
        if (request.isRelative() || specifier.startsWith("node:") || specifier.contains(":")) {
            return null;
        }
        String packageName = packageName(specifier);
        if (packageName == null) {
            return null;
        }
        String subpath = specifier.length() > packageName.length() ? specifier.substring(packageName.length() + 1) : null;
        JavaScriptModuleRequest.Kind kind = request.kind();

        for (VirtualFile directory = request.directory(); directory != null; directory = directory.getParent()) {
            VirtualFile nodeModules = directory.findChild(NpmConstants.NODE_MODULES);
            VirtualFile packageDirectory = nodeModules == null ? null : nodeModules.findFileByRelativePath(packageName);
            if (packageDirectory != null && packageDirectory.isDirectory()) {
                VirtualFile resolved = resolveInPackage(packageDirectory, subpath, kind);
                if (resolved != null) {
                    return resolved;
                }
            }
        }

        VirtualFile localPackage = findLocalPackage(request.file().getProject(), packageName);
        if (localPackage != null) {
            VirtualFile resolved = resolveInPackage(localPackage, subpath, kind);
            if (resolved != null) {
                return resolved;
            }
        }

        // the declarations of DefinitelyTyped: @types/node, @types/scope__name
        String typesName = "@types/" + (packageName.startsWith("@") ? packageName.substring(1).replace("/", "__") : packageName);
        for (VirtualFile directory = request.directory(); directory != null; directory = directory.getParent()) {
            VirtualFile nodeModules = directory.findChild(NpmConstants.NODE_MODULES);
            VirtualFile typesDirectory = nodeModules == null ? null : nodeModules.findFileByRelativePath(typesName);
            if (typesDirectory != null && typesDirectory.isDirectory()) {
                VirtualFile resolved = resolveInPackage(typesDirectory, subpath, JavaScriptModuleRequest.Kind.TYPES);
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return null;
    }

    /**
     * @return {@code vue} of {@code vue/dist/x}, {@code @scope/name} of {@code @scope/name/x}
     */
    static @Nullable String packageName(String specifier) {
        int slash = specifier.indexOf('/');
        if (specifier.startsWith("@")) {
            if (slash <= 1) {
                return null;
            }
            int second = specifier.indexOf('/', slash + 1);
            return second < 0 ? specifier : specifier.substring(0, second);
        }
        return slash < 0 ? specifier : specifier.substring(0, slash);
    }

    @RequiredReadAction
    private @Nullable VirtualFile resolveInPackage(VirtualFile packageDirectory, @Nullable String subpath, JavaScriptModuleRequest.Kind kind) {
        Map<?, ?> manifest = manifest(packageDirectory);

        Object exports = manifest == null ? null : manifest.get("exports");
        if (exports != null) {
            String target = NpmExports.resolve(exports, subpath == null ? "." : "./" + subpath, conditions(kind));
            if (target != null) {
                VirtualFile file = findFile(packageDirectory, target, kind);
                if (file != null) {
                    return file;
                }
            }
        }

        if (subpath != null) {
            return findFile(packageDirectory, subpath, kind);
        }

        if (manifest != null) {
            List<String> fields = switch (kind) {
                case TYPES -> List.of("types", "typings", "module", "main");
                case IMPORT -> List.of("module", "main");
                case REQUIRE -> List.of("main");
            };
            for (String field : fields) {
                if (manifest.get(field) instanceof String entry && !entry.isBlank()) {
                    VirtualFile file = findFile(packageDirectory, entry, kind);
                    if (file != null) {
                        return file;
                    }
                }
            }
        }
        return findIndex(packageDirectory, kind);
    }

    private static List<String> conditions(JavaScriptModuleRequest.Kind kind) {
        return switch (kind) {
            case TYPES -> List.of("types", "typings", "import", "module", "require", "node", "default");
            case IMPORT -> List.of("import", "module", "browser", "node", "default");
            case REQUIRE -> List.of("require", "node", "default");
        };
    }

    /**
     * A file of the package: as written, else with an extension, else a directory with its package.json or index;
     * for TypeScript the declarations next to a script ({@code x.js} - {@code x.d.ts}) first
     */
    @RequiredReadAction
    private @Nullable VirtualFile findFile(VirtualFile packageDirectory, String relativePath, JavaScriptModuleRequest.Kind kind) {
        String path = relativePath.startsWith("./") ? relativePath.substring(2) : relativePath;
        if (path.isEmpty() || path.equals(".")) {
            return findIndex(packageDirectory, kind);
        }
        if (kind == JavaScriptModuleRequest.Kind.TYPES) {
            String withoutScript = stripScriptExtension(path);
            if (withoutScript != null) {
                for (String extension : List.of(".d.ts", ".d.mts", ".d.cts", ".ts")) {
                    VirtualFile declarations = packageDirectory.findFileByRelativePath(withoutScript + extension);
                    if (declarations != null && !declarations.isDirectory()) {
                        return declarations;
                    }
                }
            }
        }
        VirtualFile exact = packageDirectory.findFileByRelativePath(path);
        if (exact != null && !exact.isDirectory()) {
            return exact;
        }
        for (String extension : extensions(kind)) {
            VirtualFile file = packageDirectory.findFileByRelativePath(path + extension);
            if (file != null && !file.isDirectory()) {
                return file;
            }
        }
        if (exact != null) {
            Map<?, ?> manifest = manifest(exact);
            if (manifest != null && manifest.get(kind == JavaScriptModuleRequest.Kind.TYPES ? "types" : "main") instanceof String entry) {
                VirtualFile file = findFile(exact, entry, kind);
                if (file != null) {
                    return file;
                }
            }
            return findIndex(exact, kind);
        }
        return null;
    }

    private static @Nullable VirtualFile findIndex(VirtualFile directory, JavaScriptModuleRequest.Kind kind) {
        for (String extension : extensions(kind)) {
            VirtualFile index = directory.findChild("index" + extension);
            if (index != null && !index.isDirectory()) {
                return index;
            }
        }
        return null;
    }

    private static List<String> extensions(JavaScriptModuleRequest.Kind kind) {
        return kind == JavaScriptModuleRequest.Kind.TYPES ? TYPES_EXTENSIONS : SCRIPT_EXTENSIONS;
    }

    private static @Nullable String stripScriptExtension(String path) {
        for (String extension : List.of(".js", ".mjs", ".cjs", ".jsx")) {
            if (path.endsWith(extension)) {
                return path.substring(0, path.length() - extension.length());
            }
        }
        return null;
    }

    /**
     * The directory of a package of the project by its name: the content root of a module with that package.json
     */
    @RequiredReadAction
    private @Nullable VirtualFile findLocalPackage(Project project, String packageName) {
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
                Map<?, ?> manifest = manifest(contentRoot);
                if (manifest != null && packageName.equals(manifest.get("name"))) {
                    return contentRoot;
                }
            }
        }
        return null;
    }

    private @Nullable Map<?, ?> manifest(VirtualFile packageDirectory) {
        VirtualFile packageJson = packageDirectory.findChild(NpmConstants.PACKAGE_JSON);
        if (packageJson == null || packageJson.isDirectory()) {
            return null;
        }
        long stamp = packageJson.getModificationStamp();
        Manifest cached = myManifests.get(packageJson);
        if (cached != null && cached.stamp() == stamp) {
            return cached.json();
        }
        Map<?, ?> json;
        try {
            json = NpmPackageParser.readJson(new String(packageJson.contentsToByteArray(), StandardCharsets.UTF_8)) instanceof Map<?, ?> map
                ? map
                : null;
        }
        catch (IOException e) {
            json = null;
        }
        myManifests.put(packageJson, new Manifest(stamp, json));
        return json;
    }

    private record Manifest(long stamp, @Nullable Map<?, ?> json) {
    }
}
