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
