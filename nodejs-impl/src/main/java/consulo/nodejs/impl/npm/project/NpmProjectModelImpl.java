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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ServiceImpl;
import consulo.application.Application;
import consulo.application.concurrent.coroutine.WriteLock;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.content.library.Library;
import consulo.content.library.LibraryTable;
import consulo.disposer.Disposer;
import consulo.javascript.library.JavaScriptStandardLibrary;
import consulo.externalSystem.importing.ImportSpecBuilder;
import consulo.externalSystem.model.task.ProgressExecutionMode;
import consulo.externalSystem.service.project.ExternalSystemProjectRefresher;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.orderEntry.DependencyScope;
import consulo.module.content.layer.orderEntry.LibraryOrderEntry;
import consulo.module.content.layer.orderEntry.ModuleOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.nodejs.impl.module.extension.NodeJSMutableModuleExtension;
import consulo.nodejs.impl.npm.NpmPackageReader;
import consulo.nodejs.impl.npm.NpmSystem;
import consulo.nodejs.impl.npm.NpmUtil;
import consulo.nodejs.impl.npm.setting.NpmProjectSettings;
import consulo.nodejs.impl.npm.setting.NpmSettings;
import consulo.nodejs.npm.NpmConstants;
import consulo.nodejs.packages.detect.NpmModuleExtensions;
import consulo.nodejs.npm.NpmImporter;
import consulo.nodejs.npm.NpmPackage;
import consulo.nodejs.npm.NpmProjectModel;
import consulo.project.Project;
import consulo.util.concurrent.coroutine.Coroutine;
import consulo.util.concurrent.coroutine.CoroutineScope;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 2026-10-31
 */
@Singleton
@ServiceImpl
public class NpmProjectModelImpl implements NpmProjectModel {
    private static final String[] ROOT_TYPES = {BinariesOrderRootType.ID, SourcesOrderRootType.ID};

    private final Project myProject;
    private final Application myApplication;
    /**
     * The modules waiting for a sync: a sync reads the {@code package.json} when it starts, so the requests made before
     * are served by it
     */
    private final Set<Module> myPendingModules = ConcurrentHashMap.newKeySet();

    @Inject
    public NpmProjectModelImpl(Project project, Application application) {
        myProject = project;
        myApplication = application;
    }

    @RequiredReadAction
    @Override
    public boolean apply(ModifiableRootModel model, VirtualFile packageRoot, NpmPackage npmPackage) {
        Module module = model.getModule();

        enableNodeJS(module, model);

        // frameworks configure the module from the dependencies, like Maven importers
        for (NpmImporter importer : myApplication.getExtensionList(NpmImporter.class)) {
            if (importer.isApplicable(npmPackage)) {
                importer.process(module, model, npmPackage, packageRoot);
            }
        }

        Map<String, Module> localPackages = findLocalPackages(module);
        syncModuleDependencies(model, npmPackage, localPackages);
        syncLibraries(model, packageRoot, npmPackage, localPackages.keySet());
        return model.isChanged();
    }

    @Override
    public void scheduleSync(Module module) {
        if (!myPendingModules.add(module)) {
            return;
        }
        // the model is taken and committed in one write action: a model taken earlier would undo the changes committed
        // meanwhile (an import)
        CoroutineScope.launchAsync(
            myProject.coroutineContext(),
            () -> Coroutine
                .first(WriteLock.<Object, Object>apply((ignored, continuation) -> sync(module)))
        );
    }

    @Override
    public void linkAndImport(String packageDirectory) {
        if (NpmUtil.findProjectSettings(myProject, packageDirectory) == null) {
            NpmProjectSettings projectSettings = new NpmProjectSettings();
            projectSettings.setExternalProjectPath(packageDirectory);
            NpmSettings.getInstance(myProject).linkProject(projectSettings);
        }
        ExternalSystemProjectRefresher.getInstance().refreshProjects(
            new ImportSpecBuilder(myProject, NpmSystem.SYSTEM_ID)
                .forceWhenUptodate()
                .use(ProgressExecutionMode.IN_BACKGROUND_ASYNC)
        );
    }

    @RequiredReadAction
    @Override
    public @Nullable VirtualFile findPackageJson(Module module) {
        for (VirtualFile contentRoot : ModuleRootManager.getInstance(module).getContentRoots()) {
            VirtualFile packageJson = contentRoot.findChild(NpmConstants.PACKAGE_JSON);
            if (packageJson != null && !packageJson.isDirectory()) {
                return packageJson;
            }
        }
        return null;
    }

    @RequiredWriteAction
    private @Nullable Object sync(Module module) {
        myPendingModules.remove(module);
        if (module.isDisposed()) {
            return null;
        }
        VirtualFile packageJson = findPackageJson(module);
        VirtualFile packageRoot = packageJson == null ? null : packageJson.getParent();
        NpmPackage npmPackage = NpmPackageReader.read(myProject, packageJson);
        if (packageRoot == null || npmPackage == null) {
            return null;
        }
        ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
        try {
            if (apply(model, packageRoot, npmPackage)) {
                model.commit();
            }
            else {
                model.dispose();
            }
        }
        catch (RuntimeException e) {
            if (!model.isDisposed()) {
                model.dispose();
            }
            throw e;
        }
        return null;
    }

    /**
     * Node.js runs npm: enabled with the bundle of the settings or the first one, if none is set yet
     */
    private static void enableNodeJS(Module module, ModifiableRootModel model) {
        NodeJSMutableModuleExtension extension = model.getExtensionWithoutCheck(NodeJSMutableModuleExtension.class);
        if (extension == null) {
            return;
        }
        if (!extension.isEnabled()) {
            extension.setEnabled(true);
        }
        NpmModuleExtensions.attachSdk(model, extension);
    }

    /**
     * A module library {@code npm: <name>} per dependency, over {@code node_modules/<name>}: only these libraries are
     * created, changed or removed - the other entries of the module (Maven, the user) are kept
     */
    /**
     * The packages of the modules of the project (the packages of the workspaces of a monorepo) by their name, without
     * the package of {@code module}
     */
    @RequiredReadAction
    private Map<String, Module> findLocalPackages(Module module) {
        Map<String, Module> result = new LinkedHashMap<>();
        for (Module other : ModuleManager.getInstance(myProject).getModules()) {
            if (other == module) {
                continue;
            }
            NpmPackage otherPackage = NpmPackageReader.read(myProject, findPackageJson(other));
            String name = otherPackage == null ? null : otherPackage.name();
            if (name != null && !name.isBlank()) {
                result.putIfAbsent(name, other);
            }
        }
        return result;
    }

    /**
     * A dependency on a package of the project is a dependency on its module: only the dependencies on the modules of
     * local packages are added or removed - the other module dependencies (Maven, the user) are kept
     */
    private static void syncModuleDependencies(ModifiableRootModel model, NpmPackage npmPackage, Map<String, Module> localPackages) {
        Map<Module, DependencyScope> wanted = new LinkedHashMap<>();
        for (String dependency : npmPackage.getAllDependencyNames()) {
            Module module = localPackages.get(dependency);
            if (module != null) {
                wanted.put(module, npmPackage.isDevOnly(dependency) ? DependencyScope.TEST : DependencyScope.COMPILE);
            }
        }
        Set<Module> local = Set.copyOf(localPackages.values());
        for (OrderEntry orderEntry : model.getOrderEntries()) {
            if (!(orderEntry instanceof ModuleOrderEntry moduleOrderEntry)) {
                continue;
            }
            Module module = moduleOrderEntry.getModule();
            if (module == null || !local.contains(module)) {
                continue;
            }
            DependencyScope scope = wanted.remove(module);
            if (scope == null) {
                model.removeOrderEntry(moduleOrderEntry);
            }
            else if (moduleOrderEntry.getScope() != scope) {
                moduleOrderEntry.setScope(scope);
            }
        }
        for (Map.Entry<Module, DependencyScope> entry : wanted.entrySet()) {
            ModuleOrderEntry orderEntry = model.addModuleOrderEntry(entry.getKey());
            if (orderEntry.getScope() != entry.getValue()) {
                orderEntry.setScope(entry.getValue());
            }
        }
    }

    private static void syncLibraries(ModifiableRootModel model, VirtualFile packageRoot, NpmPackage npmPackage, Set<String> localPackages) {
        LibraryTable table = model.getModuleLibraryTable();
        Map<String, Library> obsolete = new LinkedHashMap<>();
        for (Library library : table.getLibraries()) {
            String name = library.getName();
            if (name != null && name.startsWith(NpmConstants.LIBRARY_PREFIX)) {
                obsolete.put(name, library);
            }
        }

        String nodeModulesUrl = packageRoot.getUrl() + "/" + NpmConstants.NODE_MODULES;
        for (String dependency : npmPackage.getAllDependencyNames()) {
            if (localPackages.contains(dependency)) {
                // a module dependency
                continue;
            }
            String libraryName = NpmConstants.LIBRARY_PREFIX + dependency;
            Library library = obsolete.remove(libraryName);
            if (library == null) {
                library = table.createLibrary(libraryName);
            }

            VirtualFile installed = findInstalled(packageRoot, dependency);
            syncLibrary(library, installed != null ? installed.getUrl() : nodeModulesUrl + "/" + dependency, installed);

            LibraryOrderEntry orderEntry = model.findLibraryOrderEntry(library);
            DependencyScope scope = npmPackage.isDevOnly(dependency) ? DependencyScope.TEST : DependencyScope.COMPILE;
            if (orderEntry != null && orderEntry.getScope() != scope) {
                orderEntry.setScope(scope);
            }
        }

        for (Library library : obsolete.values()) {
            table.removeLibrary(library);
        }
    }

    /**
     * The installed package, like Node.js resolves it: in the {@code node_modules} of the package, else of the
     * directories above - the workspaces of a monorepo share the {@code node_modules} of the root
     */
    private static @Nullable VirtualFile findInstalled(VirtualFile packageRoot, String dependency) {
        for (VirtualFile directory = packageRoot; directory != null; directory = directory.getParent()) {
            VirtualFile nodeModules = directory.findChild(NpmConstants.NODE_MODULES);
            VirtualFile installed = nodeModules == null ? null : nodeModules.findFileByRelativePath(dependency);
            if (installed != null && installed.isDirectory()) {
                return installed;
            }
        }
        return null;
    }

    /**
     * @param installed the package directory, {@code null} if the package is not installed yet: the root is a URL, the
     *                  library sees the package once {@code npm install} creates it
     */
    private static void syncLibrary(Library library, String rootUrl, @Nullable VirtualFile installed) {
        Library.ModifiableModel libraryModel = library.getModifiableModel();
        try {
            for (String rootType : ROOT_TYPES) {
                List<String> urls = Arrays.asList(libraryModel.getUrls(rootType));
                if (!urls.equals(List.of(rootUrl))) {
                    for (String url : urls) {
                        libraryModel.removeRoot(url, rootType);
                    }
                    libraryModel.addRoot(rootUrl, rootType);
                }
            }

            List<String> excluded = new ArrayList<>();
            if (installed != null) {
                for (VirtualFile child : installed.getChildren()) {
                    if (!child.isDirectory()) {
                        continue;
                    }
                    // the lib.*.d.ts of the typescript package are the standard library: it comes with the SDK of the
                    // module, filtered by its language level and environment - not all of it, twice
                    if (NpmConstants.PACKAGE_EXCLUDED_DIRECTORIES.contains(child.getName()) || JavaScriptStandardLibrary.isLibDirectory(child)) {
                        excluded.add(child.getUrl());
                    }
                }
            }
            List<String> current = Arrays.asList(libraryModel.getExcludedRootUrls());
            if (!current.equals(excluded)) {
                for (String url : current) {
                    libraryModel.removeExcludedRoot(url);
                }
                for (String url : excluded) {
                    libraryModel.addExcludedRoot(url);
                }
            }

            if (libraryModel.isChanged()) {
                libraryModel.commit();
            }
            else {
                Disposer.dispose(libraryModel);
            }
        }
        catch (RuntimeException e) {
            Disposer.dispose(libraryModel);
            throw e;
        }
    }
}
