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
package consulo.nodejs.run;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.localize.LocalizeValue;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * A Node.js bundle used instead of the one of the configuration module.
 */
public class NodeJSAlternativeBundleFragment<T extends NodeJSConfigurationBase> implements RunConfigurationFragment<T> {
    @Override
    public String getId() {
        return "nodejsAlternativeBundle";
    }

    @Override
    public LocalizeValue getName() {
        return NodeJSLocalize.runConfigurationFragmentAlternativeBundle();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.MODULE + 10;
    }

    @Override
    public boolean isVisibleByDefault(T target) {
        return target.isUseAlternativeBundle();
    }

    @Override
    public void applyHidden(T target) {
        target.setUseAlternativeBundle(false);
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<T> createEditor(Project project, Disposable parent) {
        BundleBox bundle = BundleBoxBuilder.create(parent)
            .withSdkTypeFilter(sdkType -> sdkType == NodeJSBundleType.getInstance())
            .withNoneItem()
            .build();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(NodeJSLocalize.runConfigurationUseAlternativeBundle(), bundle.getComponent());
            }

            @RequiredUIAccess
            @Override
            public void reset(T target) {
                bundle.setSelectedBundle(target.getAlternativeBundleName());
            }

            @RequiredUIAccess
            @Override
            public void apply(T target) {
                target.setUseAlternativeBundle(true);
                target.setAlternativeBundleName(bundle.getSelectedBundleName());
            }
        };
    }
}
