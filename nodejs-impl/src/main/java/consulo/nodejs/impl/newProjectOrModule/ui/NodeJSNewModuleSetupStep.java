/*
 * Copyright 2013-2017 consulo.io
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

package consulo.nodejs.impl.newProjectOrModule.ui;

import consulo.content.bundle.SdkTable;
import consulo.disposer.Disposable;
import consulo.ide.newModule.ui.UnifiedProjectOrModuleNameStep;
import consulo.localize.LocalizeValue;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.nodejs.impl.newProjectOrModule.NodeJSNewModuleWizardContext;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17.12.2015
 */
public class NodeJSNewModuleSetupStep<C extends NodeJSNewModuleWizardContext> extends UnifiedProjectOrModuleNameStep<C>
{
	private BundleBox myBundleBox;

	public NodeJSNewModuleSetupStep(@Nonnull C context)
	{
		super(context);
	}

	@RequiredUIAccess
	@Override
	protected void extend(@Nonnull FormBuilder builder, @Nonnull Disposable uiDisposable)
	{
		super.extend(builder, uiDisposable);

		BundleBoxBuilder boxBuilder = BundleBoxBuilder.create(uiDisposable);
		boxBuilder.withSdkTypeFilterByType(NodeJSBundleType.getInstance());

		builder.addLabeled(LocalizeValue.localizeTODO("Bundle:"), (myBundleBox = boxBuilder.build()).getComponent());

		ComboBox<BundleBox.BundleBoxItem> component = myBundleBox.getComponent();
		if(component.getListModel().getSize() > 0)
		{
			component.setValueByIndex(0);
		}
	}

	@Override
	public void onStepLeave(@Nonnull C context)
	{
		super.onStepLeave(context);

		String selectedBundleName = myBundleBox.getSelectedBundleName();
		if(selectedBundleName != null)
		{
			context.setSdk(SdkTable.getInstance().findSdk(selectedBundleName));
		}
	}
}
