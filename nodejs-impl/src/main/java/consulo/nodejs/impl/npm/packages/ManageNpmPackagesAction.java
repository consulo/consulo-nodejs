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

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.nodejs.npm.NpmProjectModel;
import consulo.repository.ui.PackagesEditorManager;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;
import consulo.ui.ex.action.DumbAwareAction;
import consulo.ui.ex.action.IdeActions;

@ActionImpl(id = "Npm.ManagePackages", parents = @ActionParentRef(@ActionRef(id = IdeActions.GROUP_PROJECT_VIEW_POPUP)))
public class ManageNpmPackagesAction extends DumbAwareAction implements AnActionWithSyncUpdate {
    public ManageNpmPackagesAction() {
        super(LocalizeValue.localizeTODO("Manage npm Packages"), LocalizeValue.empty(), NodeJSApiIconGroup.npm());
    }

    @RequiredUIAccess
    @Override
    public void actionPerformed(AnActionEvent e) {
        Module module = e.getData(Module.KEY);
        if (module == null) {
            return;
        }
        PackagesEditorManager.getInstance(module.getProject()).open(NpmPackageManagementServiceProvider.class, module.getName());
    }

    @Override
    public void update(AnActionEvent e) {
        Module module = e.getData(Module.KEY);
        e.getPresentation().setEnabledAndVisible(module != null && NpmProjectModel.getInstance(module.getProject()).findPackageJson(module) != null);
    }
}
