/*
 * Copyright 2013-2026 consulo.io
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

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.localize.LocalizeValue;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.project.Project;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;

public class NpxCommandFragment implements RunConfigurationFragment<NpxConfiguration> {
    @Override
    public String getId() {
        return "npxCommand";
    }

    @Override
    public LocalizeValue getName() {
        return NodeJSLocalize.runConfigurationFragmentCommand();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.FIRST;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<NpxConfiguration> createEditor(Project project, Disposable parent) {
        TextBox command = TextBox.create();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(NodeJSLocalize.runConfigurationCommandLabel(), command);
            }

            @RequiredUIAccess
            @Override
            public void reset(NpxConfiguration target) {
                command.setValue(StringUtil.notNullize(target.getNpxCommand()), false);
            }

            @RequiredUIAccess
            @Override
            public void apply(NpxConfiguration target) {
                target.setNpxCommand(command.getValue());
            }
        };
    }
}
