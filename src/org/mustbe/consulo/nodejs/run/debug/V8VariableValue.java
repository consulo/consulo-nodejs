package org.mustbe.consulo.nodejs.run.debug;

import org.chromium.sdk.JsVariable;
import org.jetbrains.annotations.NotNull;
import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class V8VariableValue extends XNamedValue
{
	@NotNull
	private final JsVariable myJsVariable;

	public V8VariableValue(@NotNull JsVariable jsVariable)
	{
		super(jsVariable.getName());
		myJsVariable = jsVariable;
	}

	@Override
	public void computePresentation(@NotNull XValueNode xValueNode, @NotNull XValuePlace xValuePlace)
	{
		xValueNode.setPresentation(AllIcons.Nodes.Variable, new XValuePresentation()
		{
			@Override
			public void renderValue(@NotNull XValueTextRenderer xValueTextRenderer)
			{
				xValueTextRenderer.renderValue(myJsVariable.getValue().getValueString());
			}
		}, true);
	}
}
