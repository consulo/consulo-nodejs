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
package consulo.nodejs.run;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;

public class NodeJSScriptFragment implements RunConfigurationFragment<NodeJSConfiguration> {
    @Override
    public String getId() {
        return "nodejsScript";
    }

    @Override
    public LocalizeValue getName() {
        return NodeJSLocalize.runConfigurationFragmentScript();
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
    public RunConfigurationFragmentEditor<NodeJSConfiguration> createEditor(Project project, Disposable parent) {
        Module[] module = new Module[1];
        FileChooserTextBoxBuilder.Controller script = NodeJSModuleRelativePathField.create(
            project,
            parent,
            NodeJSLocalize.runConfigurationSelectScriptTitle(),
            NodeJSLocalize.runConfigurationSelectScriptDescription(),
            new FileChooserDescriptor(true, false, false, false, false, false),
            () -> module[0]
        );

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(NodeJSLocalize.runConfigurationScriptLabel(), script.getComponent());
            }

            @RequiredUIAccess
            @Override
            public void reset(NodeJSConfiguration target) {
                module[0] = target.getConfigurationModule().getModule();
                script.getComponent().setValue(StringUtil.notNullize(target.getScriptFilePath()), false);
            }

            @RequiredUIAccess
            @Override
            public void apply(NodeJSConfiguration target) {
                target.setScriptFilePath(script.getValue());
            }
        };
    }
}
