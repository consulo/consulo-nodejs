/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.nodejs.newProjectOrModule;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.nodejs.bundle.NodeJSBundleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.projectRoots.impl.SdkListCellRenderer;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;

/**
 * @author VISTALL
 * @since 17.12.2015
 */
public class NodeJSNewModuleBuilderPanel extends JPanel
{
	private ComboBox myComboBox;

	public NodeJSNewModuleBuilderPanel()
	{
		super(new VerticalFlowLayout());

		SdkTable sdkTable = SdkTable.getInstance();
		myComboBox = new ComboBox();
		myComboBox.setRenderer(new SdkListCellRenderer("<none>"));

		for(Sdk o : sdkTable.getAllSdks())
		{
			SdkTypeId sdkType = o.getSdkType();
			if(sdkType == NodeJSBundleType.getInstance())
			{
				myComboBox.addItem(o);
			}
		}

		add(LabeledComponent.create(myComboBox, "Bundle").setLabelLocation(BorderLayout.WEST));
	}

	@Nullable
	public Sdk getSdk()
	{
		return (Sdk) myComboBox.getSelectedItem();
	}
}
