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
package consulo.nodejs.impl.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.execution.configuration.*;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.nodejs.run.NodeJSConfiguration;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2014-03-18
 */
@ExtensionImpl
public class NodeJSConfigurationType extends ConfigurationTypeBase {
    @Nonnull
    public static NodeJSConfigurationType getInstance() {
        return Application.get().getExtensionPoint(ConfigurationType.class)
            .findExtensionOrFail(NodeJSConfigurationType.class);
    }

    public NodeJSConfigurationType() {
        super("NodeJSConfigurationType", NodeJSLocalize.nodejsConfigurationName(), NodeJSApiIconGroup.nodejs());

        addFactory(new ConfigurationFactory(this) {
            @Override
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new NodeJSConfiguration("Unnamed", new RunConfigurationModule(project), this);
            }

            @Override
            public boolean isApplicable(@Nonnull Project project) {
                return ModuleExtensionHelper.getInstance(project).hasModuleExtension(NodeJSModuleExtension.class);
            }
        });
    }
}
