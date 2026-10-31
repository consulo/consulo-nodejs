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

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.ProjectData;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.nodejs.impl.npm.NpmPackageReader;
import consulo.nodejs.impl.npm.NpmUtil;
import consulo.nodejs.impl.npm.NpmWorkspaces;
import consulo.nodejs.impl.npm.setting.NpmExecutionSettings;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.nodejs.npm.NpmConstants;
import consulo.nodejs.npm.NpmImporter;
import consulo.nodejs.npm.NpmPackage;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static consulo.nodejs.impl.npm.NpmSystem.SYSTEM_ID;

/**
 * The model of a linked {@code package.json}: the npm commands and the scripts as tasks, and
 * <ul>
 * <li>a module over the package directory, with {@code node_modules} excluded - unless</li>
 * <li>the package directory is in a module of another build system (a Maven module with a frontend): the module stays
 * owned by it and is only extended by the {@link NpmPackageData}.</li>
 * </ul>
 *
 * @since 2026-10-31
 */
public class NpmProjectResolver implements ExternalSystemProjectResolver<NpmExecutionSettings> {
    @Override
    public @Nullable DataNode<ProjectData> resolveProjectInfo(ExternalSystemTaskId id,
                                                              String projectPath,
                                                              boolean isPreviewMode,
                                                              @Nullable NpmExecutionSettings settings,
                                                              ExternalSystemTaskNotificationListener listener)
        throws ExternalSystemException, IllegalArgumentException, IllegalStateException {
        Path packageDirectory = NpmUtil.toPackageDirectory(Path.of(projectPath));
        Path packageJson = packageDirectory.resolve(NpmConstants.PACKAGE_JSON);
        if (!Files.isRegularFile(packageJson)) {
            throw new IllegalArgumentException(NodeJSLocalize.npmErrorNoPackageJson(packageDirectory).get());
        }
        Project project = id.findProject();
        if (project == null) {
            throw new IllegalStateException("No project for " + id);
        }
        listener.onStatusChange(new ExternalSystemTaskNotificationEvent(id, NodeJSLocalize.npmStatusReading().get()));

        VirtualFile packageJsonFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(packageJson);
        if (packageJsonFile == null) {
            throw new IllegalArgumentException(NodeJSLocalize.npmErrorNoPackageJson(packageDirectory).get());
        }

        // the packages of the workspaces are found on disk; their package.json are read with the root's
        List<Path> workspaceDirectories = new ArrayList<>();
        List<VirtualFile> workspacePackageJsons = new ArrayList<>();
        NpmPackage rootPackage = ReadAction.compute(() -> NpmPackageReader.read(project, packageJsonFile));
        if (rootPackage == null) {
            throw new ExternalSystemException(NodeJSLocalize.npmErrorCannotRead(packageJson).get());
        }
        for (Path workspaceDirectory : NpmWorkspaces.findPackageDirectories(packageDirectory, rootPackage.workspaces())) {
            VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(workspaceDirectory.resolve(NpmConstants.PACKAGE_JSON));
            if (file != null) {
                workspaceDirectories.add(workspaceDirectory);
                workspacePackageJsons.add(file);
            }
        }

        Resolved resolved = ReadAction.compute(() -> {
            Module rootModule = NpmUtil.findModule(project, packageDirectory);
            List<NpmModule> workspaces = new ArrayList<>();
            for (int i = 0; i < workspaceDirectories.size(); i++) {
                NpmPackage workspacePackage = NpmPackageReader.read(project, workspacePackageJsons.get(i));
                if (workspacePackage == null) {
                    continue;
                }
                Path workspaceDirectory = workspaceDirectories.get(i);
                Module module = NpmUtil.findModule(project, workspaceDirectory);
                workspaces.add(new NpmModule(workspaceDirectory, module == null ? null : module.getName(), workspacePackage));
            }
            return new Resolved(
                new NpmModule(packageDirectory, rootModule == null ? null : rootModule.getName(), rootPackage),
                rootModule == null ? null : NpmUtil.getForeignOwner(rootModule),
                workspaces
            );
        });

        // the package of a module of another build system doesn't name the project: the import would rename it
        String projectName = resolved.foreignOwner() != null ? project.getName() : null;
        return createProjectNode(projectPath, resolved.root(), resolved.foreignOwner() != null, resolved.workspaces(), projectName);
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId taskId, ExternalSystemTaskNotificationListener listener) {
        return false;
    }

    /**
     * @param root          the linked package
     * @param foreignModule whether the module of the root package is owned by another build system: it is only extended
     * @param workspaces    the packages of the workspaces of the root, a module each
     * @param projectName   the name of the project data, {@code null} for the name of the root module
     */
    public static DataNode<ProjectData> createProjectNode(String linkedProjectPath,
                                                          NpmModule root,
                                                          boolean foreignModule,
                                                          List<NpmModule> workspaces,
                                                          @Nullable String projectName) {
        Set<String> moduleNames = new HashSet<>();
        // the root module is named as its directory, like the project
        String rootModuleName = root.existingModuleName() != null ? root.existingModuleName() : String.valueOf(root.directory().getFileName());
        moduleNames.add(rootModuleName);
        String directory = root.directory().toString();
        ProjectData projectData = new ProjectData(SYSTEM_ID, projectName != null ? projectName : rootModuleName, directory, linkedProjectPath);
        DataNode<ProjectData> projectNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);

        addTasks(projectNode, root.npmPackage(), linkedProjectPath, true);

        if (foreignModule) {
            projectNode.createChild(NpmPackageData.KEY, new NpmPackageData(root.npmPackage(), directory));
            return projectNode;
        }

        createModuleNode(projectNode, linkedProjectPath, root, rootModuleName);
        for (NpmModule workspace : workspaces) {
            DataNode<ModuleData> moduleNode = createModuleNode(projectNode, linkedProjectPath, workspace, workspace.moduleName(moduleNames));
            addTasks(moduleNode, workspace.npmPackage(), workspace.directory().toString(), false);
        }
        return projectNode;
    }

    private static DataNode<ModuleData> createModuleNode(DataNode<ProjectData> projectNode,
                                                         String linkedProjectPath,
                                                         NpmModule npmModule,
                                                         String moduleName) {
        Path packageDirectory = npmModule.directory();
        NpmPackage npmPackage = npmModule.npmPackage();
        String directory = packageDirectory.toString();

        ModuleData moduleData = new ModuleData(moduleName, SYSTEM_ID, moduleName, directory, linkedProjectPath);
        DataNode<ModuleData> moduleNode = projectNode.createChild(ProjectKeys.MODULE, moduleData);

        ContentRootData contentRoot = new ContentRootData(SYSTEM_ID, directory);
        Set<String> excluded = new LinkedHashSet<>();
        excluded.add(NpmConstants.NODE_MODULES);
        for (NpmImporter importer : Application.get().getExtensionList(NpmImporter.class)) {
            if (importer.isApplicable(npmPackage)) {
                importer.collectExcludedDirectories(npmPackage, excluded::add);
            }
        }
        for (String relativePath : excluded) {
            Path path = packageDirectory.resolve(relativePath).normalize();
            if (path.startsWith(packageDirectory)) {
                contentRoot.storePath(ExternalSystemSourceType.EXCLUDED, path.toString());
            }
        }
        moduleNode.createChild(ProjectKeys.CONTENT_ROOT, contentRoot);
        moduleNode.createChild(NpmPackageData.KEY, new NpmPackageData(npmPackage, directory));
        return moduleNode;
    }

    /**
     * @param taskPath the directory the tasks run in
     * @param commands whether the npm commands ({@code install}) are tasks too: only of the root, they install the
     *                 workspaces with it
     */
    private static void addTasks(DataNode<?> parent, NpmPackage npmPackage, String taskPath, boolean commands) {
        Set<String> taskNames = new LinkedHashSet<>();
        for (Map.Entry<String, String> script : npmPackage.scripts().entrySet()) {
            if (taskNames.add(script.getKey())) {
                parent.createChild(ProjectKeys.TASK, new TaskData(SYSTEM_ID, script.getKey(), taskPath, script.getValue()));
            }
        }
        if (!commands) {
            return;
        }
        for (String command : NpmConstants.COMMAND_TASKS) {
            if (taskNames.add(command)) {
                parent.createChild(ProjectKeys.TASK, new TaskData(SYSTEM_ID, command, taskPath, describeCommand(command).get()));
            }
        }
    }

    public static LocalizeValue describeCommand(String command) {
        return switch (command) {
            case NpmConstants.TASK_INSTALL -> NodeJSLocalize.npmTaskInstallDescription();
            case NpmConstants.TASK_CI -> NodeJSLocalize.npmTaskCiDescription();
            case NpmConstants.TASK_UPDATE -> NodeJSLocalize.npmTaskUpdateDescription();
            default -> LocalizeValue.empty();
        };
    }

    /**
     * A package of the model
     *
     * @param existingModuleName the module already over the package directory (of the new project wizard, of an
     *                           earlier import): it is taken over, not doubled
     */
    public record NpmModule(Path directory, @Nullable String existingModuleName, NpmPackage npmPackage) {
        /**
         * The existing module, else the name of the package ({@code @acme/ui} as {@code acme-ui}), else the directory
         */
        String moduleName(Set<String> usedNames) {
            String name = existingModuleName;
            if (name == null) {
                String packageName = npmPackage.name();
                name = packageName == null || packageName.isBlank()
                    ? String.valueOf(directory.getFileName())
                    : StringUtil.trimStart(packageName, "@").replace('/', '-');
            }
            String unique = name;
            for (int i = 2; !usedNames.add(unique); i++) {
                unique = name + i;
            }
            return unique;
        }
    }

    private record Resolved(NpmModule root, @Nullable String foreignOwner, List<NpmModule> workspaces) {
    }
}
