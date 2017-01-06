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

import org.jetbrains.annotations.NotNull;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import consulo.ide.impl.UnzipNewModuleBuilderProcessor;
import consulo.ide.newProject.NewModuleBuilder;
import consulo.ide.newProject.NewModuleContext;
import consulo.nodejs.module.extension.NodeJSMutableModuleExtension;

/**
 * @author VISTALL
 * @since 17.12.2015
 */
public class NodeJSNewModuleBuilder implements NewModuleBuilder
{
	@Override
	public void setupContext(@NotNull NewModuleContext context)
	{
		NewModuleContext.Group group = context.createGroup("nodejs", "Node.js");

		group.add("Console Application", AllIcons.RunConfigurations.Application, new UnzipNewModuleBuilderProcessor<NodeJSNewModuleBuilderPanel>("/moduleTemplates/#NodeJSHelloWorld.zip")
		{
			@NotNull
			@Override
			public NodeJSNewModuleBuilderPanel createConfigurationPanel()
			{
				return new NodeJSNewModuleBuilderPanel();
			}

			@Override
			public void setupModule(@NotNull NodeJSNewModuleBuilderPanel panel, @NotNull ContentEntry contentEntry, @NotNull ModifiableRootModel modifiableRootModel)
			{
				unzip(modifiableRootModel);

				NodeJSMutableModuleExtension nodeJSModuleExtension = modifiableRootModel.getExtensionWithoutCheck(NodeJSMutableModuleExtension.class);
				assert nodeJSModuleExtension != null;

				nodeJSModuleExtension.setEnabled(true);

				Sdk sdk = panel.getSdk();
				if(sdk != null)
				{
					nodeJSModuleExtension.getInheritableSdk().set(null, sdk);
					modifiableRootModel.addModuleExtensionSdkEntry(nodeJSModuleExtension);
				}
			}
		});
	}
}