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
package consulo.nodejs.impl.npm.importProvider;

import consulo.disposer.Disposable;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.nodejs.impl.npm.setting.NpmProjectSettings;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * The Node.js bundle running npm for a linked package.
 *
 * @since 2026-10-31
 */
public class NpmProjectSettingsConfigurable extends AbstractExternalProjectSettingsConfigurable<NpmProjectSettings> {
    private @Nullable BundleBox myBundleBox;

    public NpmProjectSettingsConfigurable(NpmProjectSettings settings, ExternalSystemSettingsPlace place) {
        super(settings, place);
    }

    @Override
    @RequiredUIAccess
    protected Component createExtraUIComponent(Disposable uiDisposable) {
        BundleBox bundleBox = BundleBoxBuilder.create(uiDisposable)
            .withSdkTypeFilterByType(NodeJSBundleType.getInstance())
            .withNoneItem(NodeJSLocalize.npmSettingsBundleDefault())
            .build();
        String sdkName = getSettings().getNodeSdkName();
        if (sdkName == null) {
            bundleBox.setSelectedNoneBundle();
        }
        else {
            bundleBox.setSelectedBundle(sdkName);
        }
        myBundleBox = bundleBox;
        return FormBuilder.create()
            .addLabeled(NodeJSLocalize.npmSettingsBundleLabel(), bundleBox.getComponent())
            .build();
    }

    @Override
    @RequiredUIAccess
    protected boolean isExtraModified() {
        return myBundleBox != null && !Objects.equals(myBundleBox.getSelectedBundleName(), getSettings().getNodeSdkName());
    }

    @Override
    @RequiredUIAccess
    protected void applyExtra() {
        if (myBundleBox != null) {
            getSettings().setNodeSdkName(myBundleBox.getSelectedBundleName());
        }
    }
}
