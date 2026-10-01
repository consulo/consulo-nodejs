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

package consulo.nodejs.run;

import consulo.disposer.Disposable;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;

/**
 * @author VISTALL
 * @since 04.12.2015
 */
public class NodeJSConfigurationPanel extends NodeJSConfigurationPanelBase<NodeJSConfiguration> {
    private final FileChooserTextBoxBuilder.Controller myScriptTextField;

    @RequiredUIAccess
    public NodeJSConfigurationPanel(Project project, Disposable uiDisposable) {
        super(project, uiDisposable);

        myScriptTextField = createModuleRelativePathField(NodeJSLocalize.runConfigurationSelectScriptTitle(),
            NodeJSLocalize.runConfigurationSelectScriptDescription(),
            new FileChooserDescriptor(true, false, false, false, false, false));
    }

    @Override
    @RequiredUIAccess
    protected void addBefore(FormBuilder builder) {
        builder.addLabeled(NodeJSLocalize.runConfigurationScriptLabel(), myScriptTextField.getComponent());

        addVmParameters(builder);
    }

    @Override
    @RequiredUIAccess
    protected void addAfter(FormBuilder builder) {
        addModuleAndBundle(builder);
    }

    @Override
    @RequiredUIAccess
    public void apply(NodeJSConfiguration configuration) {
        super.apply(configuration);

        configuration.setScriptFilePath(myScriptTextField.getValue());
    }

    @Override
    @RequiredUIAccess
    public void reset(NodeJSConfiguration configuration) {
        super.reset(configuration);

        resetPathField(myScriptTextField, configuration.getScriptFilePath());
    }
}
