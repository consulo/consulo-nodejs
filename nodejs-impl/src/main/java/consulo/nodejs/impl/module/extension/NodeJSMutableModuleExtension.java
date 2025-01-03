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

package consulo.nodejs.impl.module.extension;

import consulo.content.bundle.Sdk;
import consulo.disposer.Disposable;
import consulo.javascript.language.JavaScriptLanguageVersion;
import consulo.javascript.language.StandardJavaScriptVersions;
import consulo.javascript.module.extension.JavaScriptMutableModuleExtension;
import consulo.language.version.LanguageVersion;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.module.ui.extension.ModuleExtensionBundleBoxBuilder;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;
import consulo.ui.util.LabeledComponents;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;

/**
 * @author VISTALL
 * @since 14.03.14
 */
public class NodeJSMutableModuleExtension extends NodeJSModuleExtension implements JavaScriptMutableModuleExtension<NodeJSModuleExtension>
{
	public NodeJSMutableModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@Nonnull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getInheritableSdk()
	{
		return (MutableModuleInheritableNamedPointer<Sdk>) super.getInheritableSdk();
	}

	@RequiredUIAccess
	@Nullable
	@Override
	public Component createConfigurationComponent(@Nonnull Disposable disposable, @Nonnull Runnable runnable)
	{
		VerticalLayout layout = VerticalLayout.create();
		layout.add(ModuleExtensionBundleBoxBuilder.createAndDefine(this, disposable, runnable).build());

		List<JavaScriptLanguageVersion> validLanguageVersions = StandardJavaScriptVersions.getInstance().getValidLanguageVersions();

		ComboBox<JavaScriptLanguageVersion> langVersionBox = ComboBox.create(validLanguageVersions);
		langVersionBox.addValueListener(e -> setLanguageVersion(e.getValue()));
		langVersionBox.setValue((JavaScriptLanguageVersion) getLanguageVersion());
		langVersionBox.setRender((presentation, i, version) -> presentation.append(version.getPresentableName()));

		layout.add(LabeledComponents.leftFilled("Language Version", langVersionBox));

		return layout;
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@Nonnull NodeJSModuleExtension nodeJSModuleExtension)
	{
		return isModifiedImpl(nodeJSModuleExtension) || myLanguageVersion != nodeJSModuleExtension.getLanguageVersion();
	}

	@Override
	public void setLanguageVersion(@Nonnull LanguageVersion languageVersion)
	{
		myLanguageVersion = languageVersion;
	}
}
