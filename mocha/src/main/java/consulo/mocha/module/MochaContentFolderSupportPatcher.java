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

package consulo.mocha.module;

import java.util.Set;

import javax.annotation.Nonnull;

import com.intellij.openapi.roots.ModifiableRootModel;
import consulo.mocha.module.extension.MochaModuleExtension;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.roots.ContentFolderSupportPatcher;
import consulo.roots.ContentFolderTypeProvider;
import consulo.roots.impl.TestContentFolderTypeProvider;


/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaContentFolderSupportPatcher implements ContentFolderSupportPatcher
{
	@Override
	public void patch(@Nonnull ModifiableRootModel model, @Nonnull Set<ContentFolderTypeProvider> set)
	{
		if(model.getExtension(MochaModuleExtension.class) != null && model.getExtension(NodeJSModuleExtension.class) != null)
		{
			set.add(TestContentFolderTypeProvider.getInstance());
		}
	}
}
