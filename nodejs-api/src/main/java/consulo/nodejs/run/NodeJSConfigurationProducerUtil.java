package consulo.nodejs.run;

import com.intellij.lang.javascript.psi.JSFile;
import consulo.execution.action.ConfigurationContext;
import consulo.javascript.language.JavaScriptFileType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nullable;
import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 24-Dec-17
 */
public class NodeJSConfigurationProducerUtil
{
	@Nullable
	public static VirtualFile findExecutableFile(ConfigurationContext configurationContext, @Nullable Predicate<JSFile> condition)
	{
		PsiElement psiLocation = configurationContext.getPsiLocation();
		PsiFile psiFile = psiLocation == null ? null : psiLocation.getContainingFile();
		if(psiFile instanceof JSFile && psiFile.getFileType() == JavaScriptFileType.INSTANCE)
		{
			if(condition != null && !condition.test((JSFile) psiFile))
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

}
