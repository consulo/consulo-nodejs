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
package consulo.mocha.run;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.mocha.localize.MochaLocalize;
import consulo.module.Module;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.nodejs.run.NodeJSModuleRelativePathField;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.RadioGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.HorizontalLayout;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;

/**
 * Runs the tests of a directory or of a single file.
 */
public class MochaTargetFragment implements RunConfigurationFragment<MochaConfiguration> {
    @Override
    public String getId() {
        return "mochaTarget";
    }

    @Override
    public LocalizeValue getName() {
        return MochaLocalize.runConfigurationFragmentTarget();
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
    public RunConfigurationFragmentEditor<MochaConfiguration> createEditor(Project project, Disposable parent) {
        Module[] module = new Module[1];
        RadioGroup<MochaConfiguration.TargetType> targetType = RadioGroup.create();
        HorizontalLayout targetTypes = HorizontalLayout.create();
        targetTypes.add(targetType.newButton(MochaLocalize.runConfigurationDirectory(), MochaConfiguration.TargetType.DIRECTORY));
        targetTypes.add(targetType.newButton(MochaLocalize.runConfigurationFile(), MochaConfiguration.TargetType.FILE));

        Label directoryLabel = Label.create(LocalizeValue.join(MochaLocalize.runConfigurationDirectory(), LocalizeValue.colon()));
        FileChooserTextBoxBuilder.Controller directory = NodeJSModuleRelativePathField.create(
            project,
            parent,
            NodeJSLocalize.runConfigurationSelectScriptTitle(),
            MochaLocalize.runConfigurationSelectScriptDirectory(),
            new FileChooserDescriptor(false, true, false, false, false, false),
            () -> module[0]
        );
        Label fileLabel = Label.create(LocalizeValue.join(MochaLocalize.runConfigurationFile(), LocalizeValue.colon()));
        FileChooserTextBoxBuilder.Controller file = NodeJSModuleRelativePathField.create(
            project,
            parent,
            NodeJSLocalize.runConfigurationSelectScriptTitle(),
            MochaLocalize.runConfigurationSelectScriptFile(),
            new FileChooserDescriptor(true, false, false, false, false, false),
            () -> module[0]
        );

        FormBuilder builder = FormBuilder.create();
        builder.addLabeled(MochaLocalize.runConfigurationTestInLabel(), targetTypes);
        builder.addLabeled(directoryLabel, directory.getComponent());
        builder.addLabeled(fileLabel, file.getComponent());
        Component form = builder.build();

        Runnable update = () -> {
            boolean isDirectory = targetType.getValue() != MochaConfiguration.TargetType.FILE;
            directoryLabel.setVisible(isDirectory);
            directory.getComponent().setVisible(isDirectory);
            fileLabel.setVisible(!isDirectory);
            file.getComponent().setVisible(!isDirectory);
        };
        targetType.addValueListener(value -> update.run());

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addBottom(form);
            }

            @RequiredUIAccess
            @Override
            public void reset(MochaConfiguration target) {
                module[0] = target.getConfigurationModule().getModule();
                targetType.setValue(target.getTargetType(), false);
                directory.getComponent().setValue(StringUtil.notNullize(target.getDirectoryPath()), false);
                file.getComponent().setValue(StringUtil.notNullize(target.getFilePath()), false);
                update.run();
            }

            @RequiredUIAccess
            @Override
            public void apply(MochaConfiguration target) {
                target.setTargetType(targetType.getValue() == MochaConfiguration.TargetType.FILE
                    ? MochaConfiguration.TargetType.FILE
                    : MochaConfiguration.TargetType.DIRECTORY);
                target.setDirectoryPath(directory.getValue());
                target.setFilePath(file.getValue());
            }
        };
    }
}
