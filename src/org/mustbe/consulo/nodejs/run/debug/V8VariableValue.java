package org.mustbe.consulo.nodejs.run.debug;

import org.chromium.sdk.JsValue;
import org.chromium.sdk.JsVariable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
			@Nullable
			@Override
			public String getType()
			{
				JsValue value = myJsVariable.getValue();
				switch(value.getType())
				{
					case TYPE_NUMBER:
					case TYPE_STRING:
					case TYPE_DATE:
					case TYPE_REGEXP:
					case TYPE_UNDEFINED:
					case TYPE_BOOLEAN:
						return null;
					default:
						String name = value.getType().name();
						return name.substring(5, name.length()).toLowerCase();
				}
			}

			@Override
			public void renderValue(@NotNull XValueTextRenderer xValueTextRenderer)
			{
				switch(myJsVariable.getValue().getType())
				{

					case TYPE_OBJECT:
						break;
					case TYPE_NUMBER:
						xValueTextRenderer.renderNumericValue(myJsVariable.getValue().getValueString());
						break;
					case TYPE_STRING:
						xValueTextRenderer.renderStringValue(myJsVariable.getValue().getValueString());
						break;
					case TYPE_FUNCTION:
						break;
					case TYPE_BOOLEAN:
						xValueTextRenderer.renderKeywordValue(myJsVariable.getValue().getValueString());
						break;
					case TYPE_ERROR:
						break;
					case TYPE_REGEXP:
						xValueTextRenderer.renderStringValue(myJsVariable.getValue().getValueString());
						break;
					case TYPE_DATE:
						xValueTextRenderer.renderValue(myJsVariable.getValue().getValueString());
						break;
					case TYPE_ARRAY:
						break;
					case TYPE_UNDEFINED:
						xValueTextRenderer.renderKeywordValue("undefined");
						break;
					case TYPE_NULL:
						break;
				}
			}
		}, true);
	}
}
