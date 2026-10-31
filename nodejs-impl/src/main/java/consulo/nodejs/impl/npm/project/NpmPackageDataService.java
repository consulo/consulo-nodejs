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

import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.nodejs.impl.npm.NpmUtil;
import consulo.nodejs.npm.NpmProjectModel;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Extends the modules with their {@code package.json} ({@link NpmProjectModel#apply}) - the modules of npm and the
 * modules of another build system holding a linked package.
 *
 * @since 2026-10-31
 */
@ExtensionImpl
public class NpmPackageDataService implements ProjectDataService<NpmPackageData, Module> {
    private static final Logger LOG = Logger.getInstance(NpmPackageDataService.class);

    @Override
    public Key<NpmPackageData> getTargetDataKey() {
        return NpmPackageData.KEY;
    }

    @Override
    public void importData(Collection<DataNode<NpmPackageData>> toImport, Project project, boolean synchronous) {
        if (toImport.isEmpty()) {
            return;
        }
        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (DataNode<NpmPackageData> node : toImport) {
                    NpmPackageData data = node.getData();
                    DataNode<ModuleData> moduleNode = ExternalSystemApiUtil.findParent(node, ProjectKeys.MODULE);
                    Module module = moduleNode != null
                        ? moduleManager.findModuleByName(moduleNode.getData().getInternalName())
                        : NpmUtil.findModule(project, Path.of(data.getPackageDirectory()));
                    VirtualFile packageRoot = LocalFileSystem.getInstance().findFileByPath(data.getPackageDirectory());
                    if (module == null || packageRoot == null) {
                        LOG.warn("npm package is not imported, there is no module for " + data.getPackageDirectory());
                        continue;
                    }
                    configure(project, module, packageRoot, data);
                }
            }
        });
    }

    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }

    @RequiredWriteAction
    private static void configure(Project project, Module module, VirtualFile packageRoot, NpmPackageData data) {
        ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
        try {
            if (NpmProjectModel.getInstance(project).apply(model, packageRoot, data.getPackage())) {
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
    }
}
