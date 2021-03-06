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

import javax.annotation.Nonnull;

import org.jdom.Element;
import com.intellij.openapi.projectRoots.SdkType;
import consulo.annotation.access.RequiredReadAction;
import consulo.javascript.lang.StandardJavaScriptVersions;
import consulo.javascript.module.extension.JavaScriptModuleExtension;
import consulo.lang.LanguageVersion;
import consulo.module.extension.impl.ModuleExtensionWithSdkImpl;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 14.03.14
 */
public class NodeJSModuleExtension extends ModuleExtensionWithSdkImpl<NodeJSModuleExtension> implements JavaScriptModuleExtension<NodeJSModuleExtension>
{
	protected LanguageVersion myLanguageVersion = StandardJavaScriptVersions.getInstance().getDefaultVersion();

	public NodeJSModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer rootModel)
	{
		super(id, rootModel);
	}

	@RequiredReadAction
	@Override
	protected void loadStateImpl(@Nonnull Element element)
	{
		super.loadStateImpl(element);
		myLanguageVersion = StandardJavaScriptVersions.getInstance().findVersionById(element.getAttributeValue("language-version"));
	}

	@Override
	protected void getStateImpl(@Nonnull Element element)
	{
		super.getStateImpl(element);
		if(myLanguageVersion != StandardJavaScriptVersions.getInstance().getDefaultVersion())
		{
			element.setAttribute("language-version", myLanguageVersion.getName());
		}
	}

	@Nonnull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return NodeJSBundleType.class;
	}

	@Nonnull
	@Override
	public LanguageVersion getLanguageVersion()
	{
		return myLanguageVersion;
	}

	@RequiredReadAction
	@Override
	public void commit(@Nonnull NodeJSModuleExtension mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);
		myLanguageVersion = mutableModuleExtension.getLanguageVersion();
	}
}
