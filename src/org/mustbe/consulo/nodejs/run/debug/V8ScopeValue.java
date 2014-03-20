package org.mustbe.consulo.nodejs.run.debug;

import java.util.List;

import org.chromium.sdk.JsScope;
import org.chromium.sdk.JsVariable;
import org.jetbrains.annotations.NotNull;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.intellij.xdebugger.frame.XValueGroup;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class V8ScopeValue extends XValueGroup
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
}
