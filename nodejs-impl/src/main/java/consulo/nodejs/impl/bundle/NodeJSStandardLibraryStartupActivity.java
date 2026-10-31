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
package consulo.nodejs.impl.bundle;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.application.dumb.DumbAware;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.content.bundle.SdkTable;
import consulo.javascript.library.JavaScriptStandardLibrary;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.project.Project;
import consulo.project.startup.PostStartupActivity;
import consulo.ui.UIAccess;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Node.js bundles keep the standard library of the installed plugin: their roots are saved with the bundle, the lib
 * directory of an older plugin (or none, of a bundle made before) is replaced.
 *
 * @since 2026-10-31
 */
@ExtensionImpl
public class NodeJSStandardLibraryStartupActivity implements PostStartupActivity, DumbAware {
    private final Application myApplication;
    private final SdkTable mySdkTable;

    @Inject
    public NodeJSStandardLibraryStartupActivity(Application application, SdkTable sdkTable) {
        myApplication = application;
        mySdkTable = sdkTable;
    }

    @Override
    public void runActivity(Project project, UIAccess uiAccess) {
        VirtualFile libDirectory = JavaScriptStandardLibrary.getInstance().getLibDirectory();
        if (libDirectory == null) {
            return;
        }
        List<Sdk> outdated = new ArrayList<>();
        for (Sdk sdk : mySdkTable.getSdksOfType(NodeJSBundleType.getInstance())) {
            if (!sdk.isPredefined() && !Arrays.asList(sdk.getRootProvider().getFiles(BinariesOrderRootType.ID)).contains(libDirectory)) {
                outdated.add(sdk);
            }
        }
        if (outdated.isEmpty()) {
            return;
        }
        uiAccess.give(() -> myApplication.runWriteAction(() -> {
            for (Sdk sdk : outdated) {
                SdkModificator modificator = sdk.getSdkModificator();
                if (NodeJSBundleType.setupStandardLibrary(modificator)) {
                    modificator.commitChanges();
                }
            }
        }));
    }
}
