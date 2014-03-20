package org.mustbe.consulo.nodejs.run.debug;

import java.util.List;

import org.chromium.sdk.JsScope;
import org.chromium.sdk.JsVariable;
import org.jetbrains.annotations.NotNull;
import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class V8ScopeValue extends XNamedValue
{
	private final JsScope myVariableScope;

	public V8ScopeValue(JsScope variableScope)
	{
		super(variableScope.getType().name());
		myVariableScope = variableScope;
	}

	@Override
	public void computeChildren(@NotNull XCompositeNode node)
	{
		List<? extends JsVariable> variables = myVariableScope.getVariables();

		XValueChildrenList valueChildrenList = new XValueChildrenList();
		for(JsVariable variable : variables)
		{
			valueChildrenList.add(new V8VariableValue(variable));
		}
		node.addChildren(valueChildrenList, true);
	}

	@Override
	public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace)
	{
		xValueNode.setPresentation(AllIcons.Ide.SharedScope, new XValuePresentation()
		{
			@NotNull
			@Override
			public String getSeparator()
			{
				return "";
			}

			@Override
			public void renderValue(@NotNull XValueTextRenderer xValueTextRenderer)
			{

			}
		}, true);
	}
}
