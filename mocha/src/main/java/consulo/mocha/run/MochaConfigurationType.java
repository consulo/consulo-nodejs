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
package consulo.mocha.run;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.execution.configuration.*;
import consulo.mocha.icon.MochaIconGroup;
import consulo.mocha.localize.MochaLocalize;
import consulo.mocha.module.extension.MochaModuleExtension;
import consulo.module.extension.ModuleExtensionHelper;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2015-12-19
 */
@ExtensionImpl
public class MochaConfigurationType extends ConfigurationTypeBase {
    @Nonnull
    public static MochaConfigurationType getInstance() {
        return Application.get().getExtensionPoint(ConfigurationType.class)
            .findExtensionOrFail(MochaConfigurationType.class);
    }

    public MochaConfigurationType() {
        super("#MochaConfigurationType", MochaLocalize.mochaConfigurationName(), MochaIconGroup.mocha());

        addFactory(new ConfigurationFactory(this) {
            @Override
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new MochaConfiguration("Unnamed", new RunConfigurationModule(project), this);
            }

            @Override
            public boolean isApplicable(@Nonnull Project project) {
                return ModuleExtensionHelper.getInstance(project).hasModuleExtension(MochaModuleExtension.class);
            }
        });
    }
}
