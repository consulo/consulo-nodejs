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

package consulo.nodejs.newProjectOrModule;

import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.util.Conditions;
import consulo.ide.newProject.ui.ProjectOrModuleNameStep;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.roots.ui.configuration.SdkComboBox;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * @author VISTALL
 * @since 17.12.2015
 */
public class NodeJSNewModuleSetupStep extends ProjectOrModuleNameStep<NodeJSNewModuleContext>
{
	private SdkComboBox myComboBox;

	public NodeJSNewModuleSetupStep(@Nonnull NodeJSNewModuleContext context)
	{
		super(context);

		myComboBox = new SdkComboBox(SdkTable.getInstance(), Conditions.equalTo(NodeJSBundleType.getInstance()), false);

		myAdditionalContentPanel.add(LabeledComponent.create(myComboBox, "Bundle"), BorderLayout.NORTH);
	}

	@Override
	public void onStepLeave(@Nonnull NodeJSNewModuleContext context)
	{
		super.onStepLeave(context);

		context.setSdk(myComboBox.getSelectedSdk());
	}
}
