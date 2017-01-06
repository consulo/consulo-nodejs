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

package consulo.nodejs.module.extension;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.ColoredListCellRenderer;
import consulo.annotations.RequiredDispatchThread;
import consulo.extension.ui.ModuleExtensionSdkBoxBuilder;
import consulo.javascript.lang.BaseJavaScriptLanguageVersion;
import consulo.javascript.lang.StandardJavaScriptVersions;
import consulo.lang.LanguageVersion;

/**
 * @author VISTALL
 * @since 12.12.2015
 */
public class NodeJSModuleExtensionPanel extends JPanel
{
	@RequiredDispatchThread
	public NodeJSModuleExtensionPanel(final NodeJSMutableModuleExtension extension, Runnable runnable)
	{
		super(new VerticalFlowLayout(true, false));

		add(ModuleExtensionSdkBoxBuilder.createAndDefine(extension, runnable).build());

		List<BaseJavaScriptLanguageVersion> validLanguageVersions = StandardJavaScriptVersions.getValidLanguageVersions();

		ComboBox languageVersionComboBox = new ComboBox(new CollectionComboBoxModel(validLanguageVersions, extension.getLanguageVersion()));
		languageVersionComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if(e.getStateChange() == ItemEvent.SELECTED)
				{
					//noinspection unchecked
					extension.setLanguageVersion((LanguageVersion) e.getItem());
				}
			}
		});
		languageVersionComboBox.setRenderer(new ColoredListCellRenderer<BaseJavaScriptLanguageVersion>()
		{
			@Override
			protected void customizeCellRenderer(JList list, BaseJavaScriptLanguageVersion value, int index, boolean selected, boolean hasFocus)
			{
				append(value.getPresentableName());
			}
		});

		add(LabeledComponent.left(languageVersionComboBox, "Language Version"));
	}
}
