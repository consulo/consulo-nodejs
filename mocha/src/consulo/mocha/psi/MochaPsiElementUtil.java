package consulo.mocha.psi;

import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSExpressionStatement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSSourceElement;

/**
 * @author VISTALL
 * @since 19.12.2015
 */
public class MochaPsiElementUtil
{
	public static final String MOCHA = "mocha";

	public static boolean containsTestsInFiles(JSFile file)
	{
		JSSourceElement[] statements = file.getStatements();
		for(JSSourceElement statement : statements)
		{
			if(statement instanceof JSExpressionStatement)
			{
				JSExpression expression = ((JSExpressionStatement) statement).getExpression();
				if(expression instanceof JSCallExpression)
				{
					JSExpression methodExpression = ((JSCallExpression) expression).getMethodExpression();
					if(methodExpression instanceof JSReferenceExpression)
					{
						JSExpression qualifier = ((JSReferenceExpression) methodExpression).getQualifier();
						if(qualifier != null)
						{
							continue;
						}
						String referencedName = ((JSReferenceExpression) methodExpression).getReferencedName();
						if("describe".equals(referencedName))
						{
							JSArgumentList argumentList = ((JSCallExpression) expression).getArgumentList();
							if(argumentList != null && argumentList.getArguments().length == 2)
							{
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
}
