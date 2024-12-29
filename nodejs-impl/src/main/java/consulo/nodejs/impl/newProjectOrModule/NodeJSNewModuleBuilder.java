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

package consulo.nodejs.impl.newProjectOrModule;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.content.bundle.Sdk;
import consulo.ide.newModule.NewModuleBuilder;
import consulo.ide.newModule.NewModuleContext;
import consulo.ide.newModule.UnzipNewModuleBuilderProcessor;
import consulo.module.content.layer.ContentEntry;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.nodejs.impl.module.extension.NodeJSMutableModuleExtension;
import consulo.nodejs.impl.newProjectOrModule.ui.NodeJSNewModuleSetupStep;
import consulo.ui.ex.wizard.WizardStep;

import jakarta.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 17.12.2015
 */
@ExtensionImpl
public class NodeJSNewModuleBuilder implements NewModuleBuilder
{
	@Override
	public void setupContext(@Nonnull NewModuleContext context)
	{
		NewModuleContext.Group group = context.createGroup("nodejs", "Node.js");

		group.add("Console Application", AllIcons.RunConfigurations.Application, new UnzipNewModuleBuilderProcessor<NodeJSNewModuleContext>("/moduleTemplates/#NodeJSHelloWorld.zip")
		{
			@Nonnull
			@Override
			public NodeJSNewModuleContext createContext(boolean isNewProject)
			{
				return new NodeJSNewModuleContext(isNewProject);
			}

			@Override
			public void buildSteps(@Nonnull Consumer<WizardStep<NodeJSNewModuleContext>> consumer, @Nonnull NodeJSNewModuleContext context)
			{
				consumer.accept(new NodeJSNewModuleSetupStep<>(context));
			}

			@RequiredReadAction
			@Override
			public void process(@Nonnull NodeJSNewModuleContext context, @Nonnull ContentEntry contentEntry, @Nonnull ModifiableRootModel modifiableRootModel)
			{
				unzip(modifiableRootModel);

				NodeJSMutableModuleExtension nodeJSModuleExtension = modifiableRootModel.getExtensionWithoutCheck(NodeJSMutableModuleExtension.class);
				assert nodeJSModuleExtension != null;

				nodeJSModuleExtension.setEnabled(true);

				Sdk sdk = context.getSdk();
				if(sdk != null)
				{
					nodeJSModuleExtension.getInheritableSdk().set(null, sdk);
					modifiableRootModel.addModuleExtensionSdkEntry(nodeJSModuleExtension);
				}
			}
		});
	}
}