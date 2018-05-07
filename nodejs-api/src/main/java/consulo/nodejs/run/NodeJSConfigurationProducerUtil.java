package consulo.nodejs.run;

import javax.annotation.Nullable;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.nodejs.module.extension.NodeJSModuleExtension;

/**
 * @author VISTALL
 * @since 24-Dec-17
 */
public class NodeJSConfigurationProducerUtil
{
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

}
