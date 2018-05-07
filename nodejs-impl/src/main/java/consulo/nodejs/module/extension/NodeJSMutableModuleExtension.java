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

import javax.swing.JComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.openapi.projectRoots.Sdk;
import consulo.annotations.RequiredDispatchThread;
import consulo.javascript.module.extension.JavaScriptMutableModuleExtension;
import consulo.lang.LanguageVersion;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.roots.ModuleRootLayer;

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

	@Nullable
	@Override
	@RequiredDispatchThread
	public JComponent createConfigurablePanel(@Nullable Runnable runnable)
	{
		return new NodeJSModuleExtensionPanel(this, runnable);
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
