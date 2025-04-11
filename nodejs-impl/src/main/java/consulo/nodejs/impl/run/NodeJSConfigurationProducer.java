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
import consulo.execution.action.ConfigurationContext;
import consulo.execution.action.RunConfigurationProducer;
import consulo.language.psi.PsiElement;
import consulo.module.Module;
import consulo.nodejs.run.NodeJSConfiguration;
import consulo.nodejs.run.NodeJSConfigurationProducerUtil;
import consulo.util.io.FileUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.VirtualFile;

/**
 * @author VISTALL
 * @since 03.12.2015
 */
@ExtensionImpl
public class NodeJSConfigurationProducer extends RunConfigurationProducer<NodeJSConfiguration> {
    public NodeJSConfigurationProducer() {
        super(NodeJSConfigurationType.getInstance());
    }

    @Override
    protected boolean setupConfigurationFromContext(NodeJSConfiguration configuration, ConfigurationContext context, SimpleReference<PsiElement> sourceElement) {
        VirtualFile executableFile = NodeJSConfigurationProducerUtil.findExecutableFile(context, null);
        if (executableFile != null) {
            Module module = context.getModule();
            assert module != null;
            configuration.setModule(module);
            String moduleDirPath = module.getModuleDirPath();
            String path = executableFile.getPath();
            String relativePath = moduleDirPath == null ? null : FileUtil.getRelativePath(moduleDirPath, FileUtil.toSystemIndependentName(path), '/');

            configuration.setName(ObjectUtil.notNull(relativePath, path));
            configuration.setScriptFilePath(ObjectUtil.notNull(relativePath, path));
            return true;
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(NodeJSConfiguration configuration, ConfigurationContext context) {
        VirtualFile executableFile = NodeJSConfigurationProducerUtil.findExecutableFile(context, null);
        return Comparing.equal(executableFile, configuration.getScriptFile());
    }
}
