/*
 * Copyright 2013-2015 must-be.org
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

import org.jetbrains.annotations.Nullable;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.lang.javascript.JavaScriptFileType;
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

/**
 * @author VISTALL
 * @since 03.12.2015
 */
public class NodeJSConfigurationProducer extends RunConfigurationProducer<NodeJSConfiguration>
{
	protected NodeJSConfigurationProducer()
	{
		super(NodeJSConfigurationType.getInstance());
	}

	@Nullable
	public static VirtualFile findExecutableFile(ConfigurationContext configurationContext, @Nullable Condition<JSFile> condition)
	{
		PsiElement psiLocation = configurationContext.getPsiLocation();
		PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
		if(psiFile instanceof JSFile && psiFile.getFileType() == JavaScriptFileType.INSTANCE)
		{
			if(condition != null && !condition.value((JSFile) psiFile))
			{
				return null;
			}
			Module module = configurationContext.getModule();
			if(module == null)
			{
				return null;
			}
			NodeJSModuleExtension extension = ModuleUtilCore.getExtension(module, NodeJSModuleExtension.class);
			if(extension == null)
			{
				return null;
			}
			return psiFile.getVirtualFile();
		}
		return null;
	}

	@Override
	protected boolean setupConfigurationFromContext(NodeJSConfiguration configuration, ConfigurationContext context, Ref<PsiElement> sourceElement)
	{
		VirtualFile executableFile = findExecutableFile(context, null);
		if(executableFile != null)
		{
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
	public boolean isConfigurationFromContext(NodeJSConfiguration configuration, ConfigurationContext context)
	{
		VirtualFile executableFile = findExecutableFile(context, null);
		return Comparing.equal(executableFile, configuration.getScriptFile());
	}
}
