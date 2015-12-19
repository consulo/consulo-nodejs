package org.mustbe.consulo.mocha.run;

import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.mocha.psi.MochaPsiElementUtil;
import org.mustbe.consulo.nodejs.NodeJSConstants;
import org.mustbe.consulo.nodejs.packages.call.NpmRunUtil;
import org.mustbe.consulo.nodejs.run.NodeJSConfiguration;
import org.mustbe.consulo.nodejs.run.NodeJSConfigurationProducer;
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
			return module != null && NpmRunUtil.findNpmModule(module, NodeJSConstants.MOCHA) != null && MochaPsiElementUtil.containsTestsInFiles(file);
		}
	};

	public MochaConfigurationProducer()
	{
		super(MochaConfigurationType.getInstance());
	}

	@Override
	protected boolean setupConfigurationFromContext(MochaConfiguration configuration, ConfigurationContext context, Ref<PsiElement> sourceElement)
	{
		VirtualFile executableFile = NodeJSConfigurationProducer.findExecutableFile(context, ourFileCondition);
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
		VirtualFile executableFile = NodeJSConfigurationProducer.findExecutableFile(context, ourFileCondition);
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
