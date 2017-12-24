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

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtil;
import consulo.annotations.RequiredReadAction;
import consulo.mocha.module.extension.MochaModuleExtension;
import consulo.mocha.psi.MochaPsiElementUtil;
import consulo.nodejs.packages.call.NpmRunUtil;
import consulo.nodejs.run.NodeJSConfiguration;
import consulo.nodejs.run.NodeJSConfigurationProducerUtil;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaConfigurationProducer extends RunConfigurationProducer<MochaConfiguration>
{
	private static final Condition<JSFile> ourFileCondition = new Condition<JSFile>()
	{
		@Override
		@RequiredReadAction
		public boolean value(JSFile file)
		{
			Module module = ModuleUtilCore.findModuleForPsiElement(file);
			return module != null &&
					ModuleUtilCore.getExtension(module, MochaModuleExtension.class) != null &&
					NpmRunUtil.findNpmModule(module,  MochaPsiElementUtil.MOCHA) != null &&
					MochaPsiElementUtil.containsTestsInFiles(file);
		}
	};

	public MochaConfigurationProducer()
	{
		super(MochaConfigurationType.getInstance());
	}

	@Override
	protected boolean setupConfigurationFromContext(MochaConfiguration configuration, ConfigurationContext context, Ref<PsiElement> sourceElement)
	{
		VirtualFile executableFile = NodeJSConfigurationProducerUtil.findExecutableFile(context, ourFileCondition);
		if(executableFile != null)
		{
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
	public boolean isConfigurationFromContext(MochaConfiguration configuration, ConfigurationContext context)
	{
		VirtualFile executableFile = NodeJSConfigurationProducerUtil.findExecutableFile(context, ourFileCondition);
		return configuration.getTargetType() == MochaConfiguration.TargetType.FILE && Comparing.equal(executableFile, configuration.getFileOrDirectory());
	}

	@Override
	@RequiredReadAction
	public boolean shouldReplace(ConfigurationFromContext self, ConfigurationFromContext other)
	{
		RunConfiguration configuration = other.getConfiguration();
		if(configuration instanceof NodeJSConfiguration)
		{
			PsiFile containingFile = other.getSourceElement().getContainingFile();
			if(containingFile instanceof JSFile)
			{
				return ourFileCondition.value((JSFile) containingFile);
			}
		}
		return false;
	}
}
