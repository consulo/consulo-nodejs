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

import com.intellij.lang.javascript.psi.JSFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.ConfigurationFromContext;
import consulo.execution.action.RunConfigurationProducer;
import consulo.execution.configuration.RunConfiguration;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.mocha.module.extension.MochaModuleExtension;
import consulo.mocha.psi.MochaPsiElementUtil;
import consulo.module.Module;
import consulo.nodejs.packages.call.NpmRunUtil;
import consulo.nodejs.run.NodeJSConfiguration;
import consulo.nodejs.run.NodeJSConfigurationProducerUtil;
import consulo.util.io.FileUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.VirtualFile;

import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
@ExtensionImpl
public class MochaConfigurationProducer extends RunConfigurationProducer<MochaConfiguration> {
    private static final Predicate<JSFile> ourFileCondition = file ->
    {
        Module module = file.getModule();
        return module != null &&
            module.getExtension(MochaModuleExtension.class) != null &&
            NpmRunUtil.findNpmModule(module, MochaPsiElementUtil.MOCHA) != null &&
            MochaPsiElementUtil.containsTestsInFiles(file);
    };

    public MochaConfigurationProducer() {
        super(MochaConfigurationType.getInstance());
    }

    @Override
    protected boolean setupConfigurationFromContext(MochaConfiguration configuration, ConfigurationContext context, SimpleReference<PsiElement> sourceElement) {
        VirtualFile executableFile = NodeJSConfigurationProducerUtil.findExecutableFile(context, ourFileCondition);
        if (executableFile != null) {
            Module module = context.getModule();
            assert module != null;
            configuration.setModule(module);
            String moduleDirPath = module.getModuleDirPath();
            String path = executableFile.getPath();
            String relativePath = moduleDirPath == null ? null : FileUtil.getRelativePath(moduleDirPath, FileUtil.toSystemIndependentName(path), '/');

            configuration.setName(ObjectUtil.notNull(relativePath, path));
            configuration.setTargetType(MochaConfiguration.TargetType.FILE);
            configuration.setFilePath(ObjectUtil.notNull(relativePath, path));
            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(MochaConfiguration configuration, ConfigurationContext context) {
        VirtualFile executableFile = NodeJSConfigurationProducerUtil.findExecutableFile(context, ourFileCondition);
        return configuration.getTargetType() == MochaConfiguration.TargetType.FILE && Comparing.equal(executableFile, configuration.getFileOrDirectory());
    }

    @Override
    @RequiredReadAction
    public boolean shouldReplace(ConfigurationFromContext self, ConfigurationFromContext other) {
        RunConfiguration configuration = other.getConfiguration();
        if (configuration instanceof NodeJSConfiguration) {
            PsiFile containingFile = other.getSourceElement().getContainingFile();
            if (containingFile instanceof JSFile) {
                return ourFileCondition.test((JSFile) containingFile);
            }
        }
        return false;
    }
}
