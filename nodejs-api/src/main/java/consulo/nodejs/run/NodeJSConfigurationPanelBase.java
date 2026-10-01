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
import consulo.execution.ui.CommonProgramParametersLayout;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.nodejs.localize.NodeJSLocalize;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.cmd.ParametersListUtil;
import consulo.project.Project;
import consulo.ui.CheckBox;
import consulo.ui.ComboBox;
import consulo.ui.TextBox;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.TextComponentAccessor;
import consulo.ui.ex.dialog.DialogService;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import consulo.ui.util.FormBuilder;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public abstract class NodeJSConfigurationPanelBase<C extends NodeJSConfigurationBase> extends CommonProgramParametersLayout<C> {
    protected final Project myProject;
    private final Disposable myUIDisposable;

    private final MutableFlatDataModel<Module> myModuleModel = FlatDataModel.of(new ArrayList<>());

    private final TextBoxWithExpandAction myVmParametersComponent;
    private final ComboBox<Module> myModuleBox;
    private final CheckBox myUseAlternativeBundleCheckBox;
    private final BundleBox myAlternativeBundleBox;

    @RequiredUIAccess
    public NodeJSConfigurationPanelBase(Project project, Disposable uiDisposable) {
        super(project.getApplication().getInstance(DialogService.class));
        myProject = project;
        myUIDisposable = uiDisposable;

        myVmParametersComponent = TextBoxWithExpandAction.create(
            PlatformIconGroup.actionsShow(),
            NodeJSLocalize.runConfigurationVmArguments().get(),
            ParametersListUtil.DEFAULT_LINE_PARSER,
            ParametersListUtil.DEFAULT_LINE_JOINER
        );

        myModuleBox = ComboBox.create(myModuleModel);
        myModuleBox.setRender((presentation, item) ->
        {
            Module module = item.getValue();
            if (module != null) {
                presentation.withIcon(PlatformIconGroup.nodesModule());
                presentation.append(module.getName());
            }
        });

        myAlternativeBundleBox = BundleBoxBuilder.create(uiDisposable)
            .withSdkTypeFilter(sdkType -> sdkType == NodeJSBundleType.getInstance())
            .withNoneItem()
            .build();

        myUseAlternativeBundleCheckBox = CheckBox.create(NodeJSLocalize.runConfigurationUseAlternativeBundle());
        myUseAlternativeBundleCheckBox.addValueListener(event -> updateAlternativeBundleState());

        updateAlternativeBundleState();
    }

    @RequiredUIAccess
    protected void addVmParameters(FormBuilder builder) {
        builder.addLabeled(LocalizeValue.join(NodeJSLocalize.runConfigurationVmArguments(), LocalizeValue.colon()), myVmParametersComponent);
    }

    @RequiredUIAccess
    protected void addModuleAndBundle(FormBuilder builder) {
        builder.addLabeled(NodeJSLocalize.runConfigurationModuleLabel(), myModuleBox);
        builder.addLabeled(myUseAlternativeBundleCheckBox, myAlternativeBundleBox.getComponent());
    }

    @RequiredUIAccess
    protected FileChooserTextBoxBuilder.Controller createModuleRelativePathField(LocalizeValue dialogTitle,
                                                                                 LocalizeValue dialogDescription,
                                                                                 FileChooserDescriptor descriptor) {
        FileChooserTextBoxBuilder builder = FileChooserTextBoxBuilder.create(myProject);
        builder.dialogTitle(dialogTitle);
        builder.dialogDescription(dialogDescription);
        builder.fileChooserDescriptor(descriptor);
        builder.textBoxAccessor(new TextComponentAccessor<>() {
            @Override
            @RequiredUIAccess
            public String getValue(TextBox textBox) {
                return StringUtil.notNullize(textBox.getValue());
            }

            @Override
            @RequiredUIAccess
            public void setValue(TextBox textBox, String text, boolean fireListeners) {
                Module module = getSelectedModule();
                String moduleDirPath = module == null ? null : module.getModuleDirPath();
                String relativePath = moduleDirPath == null ? null : FileUtil.getRelativePath(moduleDirPath, FileUtil.toSystemIndependentName(text), '/');
                textBox.setValue(StringUtil.isEmpty(relativePath) ? text : relativePath, fireListeners);
            }
        });
        builder.uiDisposable(myUIDisposable);
        return builder.build();
    }

    @RequiredUIAccess
    protected static void resetPathField(FileChooserTextBoxBuilder.Controller field, @Nullable String path) {
        field.getComponent().setValue(StringUtil.notNullize(path));
    }

    @Nullable
    @RequiredUIAccess
    protected Module getSelectedModule() {
        return myModuleBox.getValue();
    }

    @RequiredUIAccess
    private void updateAlternativeBundleState() {
        myAlternativeBundleBox.getComponent().setEnabled(Boolean.TRUE.equals(myUseAlternativeBundleCheckBox.getValue()));
    }

    @Override
    @RequiredUIAccess
    public void apply(C configuration) {
        super.apply(configuration);

        configuration.setVmParameters(myVmParametersComponent.getValue());
        configuration.getConfigurationModule().setModule(getSelectedModule());
        configuration.setUseAlternativeBundle(Boolean.TRUE.equals(myUseAlternativeBundleCheckBox.getValue()));
        configuration.setAlternativeBundleName(StringUtil.nullize(myAlternativeBundleBox.getSelectedBundleName()));
    }

    @Override
    @RequiredUIAccess
    public void reset(C configuration) {
        super.reset(configuration);

        myVmParametersComponent.setValue(StringUtil.notNullize(configuration.getVmParameters()));

        List<Module> modules = new ArrayList<>(configuration.getValidModules());
        Module module = configuration.getConfigurationModule().getModule();
        if (module != null && !modules.contains(module)) {
            modules.add(module);
        }
        myModuleModel.replaceAll(modules);
        myModuleBox.setValue(module);

        myUseAlternativeBundleCheckBox.setValue(configuration.isUseAlternativeBundle());
        myAlternativeBundleBox.setSelectedBundle(configuration.getAlternativeBundleName());

        updateAlternativeBundleState();
    }
}
