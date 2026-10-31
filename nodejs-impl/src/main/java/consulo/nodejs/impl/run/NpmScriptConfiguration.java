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

import consulo.application.ReadAction;
import consulo.content.bundle.Sdk;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.RunConfigurationModule;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.nodejs.impl.npm.NpmUtil;
import consulo.nodejs.impl.npm.setting.NpmProjectSettings;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.nodejs.npm.NpmProjectModel;
import consulo.nodejs.run.NodeJSConfigurationBase;
import consulo.nodejs.run.NodeJSRunState;
import consulo.nodejs.run.NpmScriptRunConfiguration;
import consulo.process.ExecutionException;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a script of {@code package.json}: {@code npm run <script>} in the module directory.
 */
public class NpmScriptConfiguration extends NodeJSConfigurationBase implements NpmScriptRunConfiguration {
    public NpmScriptConfiguration(String name, RunConfigurationModule configurationModule, ConfigurationFactory factory) {
        super(name, configurationModule, factory);
    }

    @Override
    protected Class<NpmScriptConfigurationOptions> getOptionsClass() {
        return NpmScriptConfigurationOptions.class;
    }

    @Override
    public NpmScriptConfigurationOptions getOptions() {
        return (NpmScriptConfigurationOptions) super.getOptions();
    }

    @Override
    protected void convertLegacyState(LegacyRunConfigurationConverter converter) {
        super.convertLegacyState(converter);
        NpmScriptConfigurationOptions options = new NpmScriptConfigurationOptions();
        convertLegacyCommonState(converter, options);
        converter.setOptions(options);
    }

    @Override
    public List<RunConfigurationFragment<?>> createFragments() {
        List<RunConfigurationFragment<?>> fragments = new ArrayList<>();
        fragments.add(new NpmScriptFragment());
        fragments.addAll(createCommonFragments());
        return fragments;
    }

    @Nonnull
    @Override
    public List<Module> getValidModules() {
        List<Module> list = new ArrayList<>();
        NpmProjectModel npmProjectModel = NpmProjectModel.getInstance(getProject());
        for (Module module : ModuleManager.getInstance(getProject()).getModules()) {
            // a module of Node.js, or with a package.json: the scripts of a frontend (Web) run with npm too
            if (ModuleUtilCore.getExtension(module, NodeJSModuleExtension.class) != null
                || ReadAction.compute(() -> npmProjectModel.findPackageJson(module)) != null) {
                list.add(module);
            }
        }
        return list;
    }

    /**
     * The bundle of the Node.js extension, else the bundle of the npm project (a frontend module has no Node.js extension)
     */
    @Nullable
    @Override
    protected Sdk findModuleBundle(@Nonnull Module module) {
        Sdk sdk = super.findModuleBundle(module);
        if (sdk != null) {
            return sdk;
        }
        VirtualFile packageJson = ReadAction.compute(() -> NpmProjectModel.getInstance(getProject()).findPackageJson(module));
        VirtualFile packageDirectory = packageJson == null ? null : packageJson.getParent();
        NpmProjectSettings settings = packageDirectory == null ? null : NpmUtil.findProjectSettings(getProject(), packageDirectory.getPath());
        return NpmUtil.findNodeSdk(settings == null ? null : settings.getNodeSdkName(), module);
    }

    @Nonnull
    @Override
    protected NodeJSRunState createRunState(@Nullable Module module, @Nonnull Sdk targetSdk, @Nonnull Executor executor,
                                            @Nonnull ExecutionEnvironment executionEnvironment) throws ExecutionException {
        NpmScriptRunState state = new NpmScriptRunState(module, targetSdk, this);
        state.addVmArgument("run");
        state.addVmArgument(StringUtil.notNullize(getScript()));
        return state;
    }

    @Nullable
    @Override
    public String getScript() {
        return getOptions().getScript();
    }

    @Override
    public void setScript(@Nullable String script) {
        getOptions().setScript(script);
    }
}
