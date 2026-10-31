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
package consulo.nodejs.it;

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.content.ContentFolderTypeProvider;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.ExcludedContentFolderTypeProvider;
import consulo.content.library.Library;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter;
import consulo.externalSystem.model.task.ExternalSystemTaskType;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.language.content.ProductionContentFolderTypeProvider;
import consulo.module.ModifiableModuleModel;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ContentFolder;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.orderEntry.DependencyScope;
import consulo.module.content.layer.orderEntry.LibraryOrderEntry;
import consulo.module.content.layer.orderEntry.ModuleOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.nodejs.impl.npm.NpmSystem;
import consulo.nodejs.impl.npm.NpmUtil;
import consulo.nodejs.impl.npm.project.NpmPackageData;
import consulo.nodejs.impl.npm.project.NpmProjectResolver;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.nodejs.npm.NpmConstants;
import consulo.nodejs.npm.NpmPackage;
import consulo.nodejs.npm.NpmProjectModel;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(HeadlessProjectExtension.class)
public class NpmIntegrationTest {
    private static final String PACKAGE_JSON = """
        {
            "name": "shop",
            "version": "1.0.0",
            "scripts": {
                "dev": "vite",
                "build": "vite build"
            },
            "dependencies": {
                "vue": "^3.5.0",
                "@acme/ui": "^1.0.0",
                "acme-framework": "^2.0.0"
            },
            "devDependencies": {
                "mocha": "^11.0.0"
            }
        }
        """;

    @Test
    public void resolveBuildsModuleWithTasks(HeadlessProjects projects) throws Exception {
        Path directory = createPackage(projects, PACKAGE_JSON);
        Project project = projects.open(directory);

        DataNode<ProjectData> projectNode = resolve(project, directory);
        assertThat(projectNode.getData().getOwner()).isEqualTo(NpmSystem.SYSTEM_ID);

        DataNode<ModuleData> moduleNode = ExternalSystemApiUtil.find(projectNode, ProjectKeys.MODULE);
        assertThat(moduleNode).isNotNull();
        assertThat(moduleNode.getData().getExternalName()).isEqualTo(directory.getFileName().toString());

        DataNode<ContentRootData> contentRootNode = ExternalSystemApiUtil.find(moduleNode, ProjectKeys.CONTENT_ROOT);
        assertThat(contentRootNode).isNotNull();
        assertThat(paths(contentRootNode.getData(), ExternalSystemSourceType.EXCLUDED))
            .containsExactlyInAnyOrder(path(directory, "node_modules"), path(directory, ".acme"));

        DataNode<NpmPackageData> packageNode = ExternalSystemApiUtil.find(moduleNode, NpmPackageData.KEY);
        assertThat(packageNode).isNotNull();
        NpmPackage npmPackage = packageNode.getData().getPackage();
        assertThat(npmPackage.name()).isEqualTo("shop");
        assertThat(npmPackage.requires("vue")).isTrue();
        assertThat(npmPackage.isDevOnly("mocha")).isTrue();

        List<String> tasks = new ArrayList<>();
        for (DataNode<TaskData> task : ExternalSystemApiUtil.findAll(projectNode, ProjectKeys.TASK)) {
            tasks.add(task.getData().getName());
        }
        assertThat(tasks).containsExactly("dev", "build", "install", "ci", "update");
    }

    @Test
    public void tasksRunThroughNpm() {
        assertThat(NpmUtil.toArguments("install")).containsExactly("install");
        assertThat(NpmUtil.toArguments("build")).containsExactly("run", "build");
    }

    /**
     * Imported as the platform imports a refreshed project: the module of npm with the Node.js extension, a library per
     * dependency and the importers run on its model; other libraries survive a new import
     */
    @Test
    public void importConfiguresModule(HeadlessProjects projects) throws Exception {
        Path directory = createPackage(projects, PACKAGE_JSON);
        Project project = projects.open(directory);
        TestNpmImporter.PROCESSED.clear();

        importData(project, resolve(project, directory));
        HeadlessProjects.awaitIdle(project);

        String moduleName = directory.getFileName().toString();
        ReadAction.run(() -> {
            Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
            assertThat(module).isNotNull();
            assertThat(NpmUtil.isLinked(module)).isTrue();
            assertThat(module.getExtension(NodeJSModuleExtension.class)).isNotNull();

            ContentEntry[] contentEntries = ModuleRootManager.getInstance(module).getContentEntries();
            assertThat(contentEntries).hasSize(1);
            assertThat(folders(contentEntries[0], ExcludedContentFolderTypeProvider.getInstance())).contains(url(directory, "node_modules"));

            Map<String, LibraryOrderEntry> libraries = npmLibraries(module);
            assertThat(libraries.keySet()).containsExactlyInAnyOrder("npm: vue", "npm: @acme/ui", "npm: acme-framework", "npm: mocha");
            assertThat(libraries.get("npm: vue").getScope()).isEqualTo(DependencyScope.COMPILE);
            assertThat(libraries.get("npm: mocha").getScope()).isEqualTo(DependencyScope.TEST);

            Library vue = libraries.get("npm: vue").getLibrary();
            assertThat(vue).isNotNull();
            assertThat(vue.getUrls(BinariesOrderRootType.ID)).containsExactly(url(directory, "node_modules/vue"));
            assertThat(vue.getExcludedRootUrls()).containsExactly(url(directory, "node_modules/vue/test"));

            Library ui = libraries.get("npm: @acme/ui").getLibrary();
            assertThat(ui).isNotNull();
            assertThat(ui.getUrls(BinariesOrderRootType.ID)).containsExactly(url(directory, "node_modules/@acme/ui"));
        });
        assertThat(TestNpmImporter.PROCESSED).containsExactly(moduleName + ": shop @ " + moduleName);

        // a library of the user, then mocha is no more a dependency
        addModuleLibrary(project, moduleName, "user library");
        // written through the VFS, as an edit in the IDE: the PSI read by the import follows it
        VirtualFile packageJson = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory.resolve(NpmConstants.PACKAGE_JSON));
        assertThat(packageJson).isNotNull();
        byte[] content = PACKAGE_JSON.replace("\"mocha\": \"^11.0.0\"", "").getBytes(StandardCharsets.UTF_8);
        runWrite(project, () -> {
            try {
                packageJson.setBinaryContent(content);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        HeadlessProjects.awaitIdle(project);
        DataNode<ProjectData> reimported = resolve(project, directory);
        DataNode<NpmPackageData> reimportedPackage = ExternalSystemApiUtil.find(
            ExternalSystemApiUtil.find(reimported, ProjectKeys.MODULE), NpmPackageData.KEY);
        assertThat(reimportedPackage.getData().getPackage().requires("mocha")).as("resolved package.json").isFalse();
        importData(project, reimported);
        HeadlessProjects.awaitIdle(project);

        ReadAction.run(() -> {
            Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
            assertThat(module).isNotNull();
            assertThat(npmLibraries(module).keySet()).containsExactlyInAnyOrder("npm: vue", "npm: @acme/ui", "npm: acme-framework");
            assertThat(libraryNames(module)).contains("user library");
        });
    }

    /**
     * A module of another build system (a Maven module with a frontend) is only extended: its folders and libraries are
     * kept
     */
    @Test
    public void applyExtendsForeignModule(HeadlessProjects projects) throws Exception {
        Path directory = createPackage(projects, PACKAGE_JSON);
        Files.createDirectories(directory.resolve("src/main/java"));
        Project project = projects.open(directory);
        VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory);
        assertThat(root).isNotNull();
        root.refresh(false, true);

        String moduleName = "java-and-frontend";
        runWrite(project, () -> {
            ModifiableModuleModel moduleModel = ModuleManager.getInstance(project).getModifiableModel();
            Module module = moduleModel.newModule(moduleName, directory.toString());
            moduleModel.commit();
            ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
            ContentEntry contentEntry = model.addContentEntry(root);
            contentEntry.addFolder(root.getUrl() + "/src/main/java", ProductionContentFolderTypeProvider.getInstance());
            model.getModuleLibraryTable().createLibrary("Maven: org.acme:acme:1.0");
            model.commit();
        });

        Module module = ReadAction.compute(() -> ModuleManager.getInstance(project).findModuleByName(moduleName));
        assertThat(module).isNotNull();
        runWrite(project, () -> {
            NpmProjectModel npmModel = NpmProjectModel.getInstance(project);
            VirtualFile packageJson = npmModel.findPackageJson(module);
            assertThat(packageJson).isNotNull();
            NpmPackage npmPackage = consulo.nodejs.npm.NpmPackageParser.parse(packageJson);
            assertThat(npmPackage).isNotNull();
            ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
            assertThat(npmModel.apply(model, root, npmPackage)).isTrue();
            model.commit();
        });

        ReadAction.run(() -> {
            assertThat(libraryNames(module)).contains("Maven: org.acme:acme:1.0", "npm: vue", "npm: mocha");
            ContentEntry[] contentEntries = ModuleRootManager.getInstance(module).getContentEntries();
            assertThat(contentEntries).hasSize(1);
            assertThat(folders(contentEntries[0], ProductionContentFolderTypeProvider.getInstance()))
                .containsExactly(root.getUrl() + "/src/main/java");
            assertThat(module.getExtension(NodeJSModuleExtension.class)).isNotNull();
            assertThat(NpmUtil.isLinked(module)).isFalse();
        });
    }

    /**
     * A monorepo: a module per package of the workspaces, a dependency on a package of the project is a module
     * dependency, the packages installed in the node_modules of the root are found from the workspaces
     */
    @Test
    public void workspacesAreModules(HeadlessProjects projects) throws Exception {
        Path directory = projects.newDirectory();
        Files.writeString(directory.resolve(NpmConstants.PACKAGE_JSON), """
            {
                "name": "monorepo",
                "private": true,
                "workspaces": ["packages/*", "!packages/legacy"],
                "devDependencies": {"typescript": "^5.9.0"}
            }
            """);
        writePackage(directory.resolve("packages/ui"), """
            {
                "name": "@acme/ui",
                "scripts": {"build": "tsc"},
                "dependencies": {"vue": "^3.5.0"}
            }
            """);
        writePackage(directory.resolve("packages/app"), """
            {
                "name": "app",
                "scripts": {"dev": "vite"},
                "dependencies": {"@acme/ui": "*", "vue": "^3.5.0"}
            }
            """);
        writePackage(directory.resolve("packages/legacy"), """
            {"name": "legacy"}
            """);
        // npm hoists the dependencies of the workspaces to the root and links the workspaces
        Files.createDirectories(directory.resolve("node_modules/vue/dist"));
        Files.createDirectories(directory.resolve("node_modules/typescript/lib"));
        VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory);
        assertThat(root).isNotNull();
        root.refresh(false, true);

        Project project = projects.open(directory);
        DataNode<ProjectData> projectNode = resolve(project, directory);

        List<String> modules = new ArrayList<>();
        for (DataNode<ModuleData> moduleNode : ExternalSystemApiUtil.findAll(projectNode, ProjectKeys.MODULE)) {
            modules.add(moduleNode.getData().getExternalName());
        }
        String rootName = directory.getFileName().toString();
        assertThat(modules).containsExactly(rootName, "app", "acme-ui");

        DataNode<ModuleData> appNode = ExternalSystemApiUtil.find(projectNode, ProjectKeys.MODULE, node -> node.getData().getExternalName().equals("app"));
        assertThat(appNode).isNotNull();
        List<String> appTasks = new ArrayList<>();
        for (DataNode<TaskData> task : ExternalSystemApiUtil.findAll(appNode, ProjectKeys.TASK)) {
            appTasks.add(task.getData().getName() + " @ " + Path.of(task.getData().getLinkedExternalProjectPath()).getFileName());
        }
        assertThat(appTasks).containsExactly("dev @ app");

        importData(project, projectNode);
        HeadlessProjects.awaitIdle(project);

        ReadAction.run(() -> {
            ModuleManager moduleManager = ModuleManager.getInstance(project);
            Module app = moduleManager.findModuleByName("app");
            Module ui = moduleManager.findModuleByName("acme-ui");
            Module rootModule = moduleManager.findModuleByName(rootName);
            assertThat(app).isNotNull();
            assertThat(ui).isNotNull();
            assertThat(rootModule).isNotNull();

            assertThat(moduleDependencies(app)).containsExactly("acme-ui");
            assertThat(npmLibraries(app).keySet()).containsExactly("npm: vue");
            Library vue = npmLibraries(app).get("npm: vue").getLibrary();
            assertThat(vue).isNotNull();
            assertThat(vue.getUrls(BinariesOrderRootType.ID)).containsExactly(url(directory, "node_modules/vue"));

            assertThat(moduleDependencies(ui)).isEmpty();
            assertThat(npmLibraries(ui).keySet()).containsExactly("npm: vue");
            assertThat(npmLibraries(rootModule).keySet()).containsExactly("npm: typescript");
            assertThat(moduleManager.findModuleByName("legacy")).isNull();
        });
    }

    private static void writePackage(Path directory, String packageJson) throws Exception {
        Files.createDirectories(directory);
        Files.writeString(directory.resolve(NpmConstants.PACKAGE_JSON), packageJson);
    }

    private static List<String> moduleDependencies(Module module) {
        List<String> result = new ArrayList<>();
        for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
            if (orderEntry instanceof ModuleOrderEntry moduleOrderEntry) {
                result.add(moduleOrderEntry.getModuleName());
            }
        }
        return result;
    }

    private static Path createPackage(HeadlessProjects projects, String packageJson) throws Exception {
        Path directory = projects.newDirectory();
        Files.writeString(directory.resolve(NpmConstants.PACKAGE_JSON), packageJson);
        for (String child : List.of("node_modules/vue/test", "node_modules/vue/dist", "node_modules/@acme/ui/lib", "node_modules/mocha")) {
            Files.createDirectories(directory.resolve(child));
        }
        VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory);
        if (root != null) {
            root.refresh(false, true);
        }
        return directory;
    }

    private static DataNode<ProjectData> resolve(Project project, Path directory) {
        ExternalSystemTaskId id = ExternalSystemTaskId.create(NpmSystem.SYSTEM_ID, ExternalSystemTaskType.RESOLVE_PROJECT, project);
        DataNode<ProjectData> projectNode = new NpmProjectResolver().resolveProjectInfo(
            id,
            directory.toString(),
            false,
            null,
            new ExternalSystemTaskNotificationListenerAdapter() {
            }
        );
        assertThat(projectNode).isNotNull();
        return projectNode;
    }

    /**
     * What the platform does with a resolved project: the data services in a project change (write) action
     */
    private static void importData(Project project, DataNode<ProjectData> projectNode) {
        ProjectDataManager dataManager = Application.get().getInstance(ProjectDataManager.class);
        runWrite(project, () -> dataManager.importData(projectNode.getKey(), Collections.singleton(projectNode), project, true));
    }

    private static void runWrite(Project project, Runnable runnable) {
        ExternalSystemApiUtil.executeProjectChangeAction(true, new DisposeAwareProjectChange(project) {
            @Override
            public void execute() {
                runnable.run();
            }
        });
    }

    private static void addModuleLibrary(Project project, String moduleName, String libraryName) {
        runWrite(project, () -> {
            Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
            assertThat(module).isNotNull();
            ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
            model.getModuleLibraryTable().createLibrary(libraryName);
            model.commit();
        });
    }

    private static Map<String, LibraryOrderEntry> npmLibraries(Module module) {
        Map<String, LibraryOrderEntry> result = new LinkedHashMap<>();
        for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry libraryOrderEntry) {
                String name = libraryOrderEntry.getLibraryName();
                if (name != null && name.startsWith(NpmConstants.LIBRARY_PREFIX)) {
                    result.put(name, libraryOrderEntry);
                }
            }
        }
        return result;
    }

    private static List<String> libraryNames(Module module) {
        List<String> result = new ArrayList<>();
        for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
            if (orderEntry instanceof LibraryOrderEntry libraryOrderEntry) {
                @Nullable String name = libraryOrderEntry.getLibraryName();
                if (name != null) {
                    result.add(name);
                }
            }
        }
        return result;
    }

    private static List<String> paths(ContentRootData contentRoot, ExternalSystemSourceType type) {
        List<String> result = new ArrayList<>();
        for (ContentRootData.SourceRoot root : contentRoot.getPaths(type)) {
            result.add(root.getPath());
        }
        return result;
    }

    private static String path(Path directory, String relative) {
        return ExternalSystemApiUtil.toCanonicalPath(directory.resolve(relative).toString());
    }

    private static List<String> folders(ContentEntry contentEntry, ContentFolderTypeProvider type) {
        List<String> result = new ArrayList<>();
        for (ContentFolder folder : contentEntry.getFolders(folderType -> folderType == type)) {
            result.add(folder.getUrl());
        }
        return result;
    }

    private static String url(Path directory, String relative) {
        return "file://" + ExternalSystemApiUtil.toCanonicalPath(directory.resolve(relative).toString());
    }
}
