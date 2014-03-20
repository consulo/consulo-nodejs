package org.mustbe.consulo.nodejs.run.debug;

import java.io.File;
import java.util.List;

import org.chromium.sdk.CallFrame;
import org.chromium.sdk.JsScope;
import org.chromium.sdk.JsVariable;
import org.chromium.sdk.TextStreamPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class V8StackFrame extends XStackFrame
{
	private final CallFrame myCallFrame;

	public V8StackFrame(CallFrame callFrame)
	{
		myCallFrame = callFrame;
	}

	@Override
	public void computeChildren(@NotNull XCompositeNode node)
	{
		List<? extends JsScope> variableScopes = myCallFrame.getVariableScopes();

		XValueChildrenList xValueChildrenList = new XValueChildrenList();

		for(JsScope variableScope : variableScopes)
		{
			switch(variableScope.getType())
			{
				case LOCAL:
					break;
				default:
					xValueChildrenList.add(new V8ScopeValue(variableScope));
					break;
			}
		}
		for(JsScope variableScope : variableScopes)
		{
			switch(variableScope.getType())
			{
				case LOCAL:
					for(JsVariable jsVariable : variableScope.getVariables())
					{
						xValueChildrenList.add(new V8VariableValue(jsVariable));
					}
					break;
			}
		}
		node.addChildren(xValueChildrenList, true);
	}

	@Override
	public void customizePresentation(ColoredTextContainer component)
	{
		TextStreamPosition statementStartPosition = myCallFrame.getStatementStartPosition();

		XSourcePosition position = getSourcePosition();
		if(position != null)
		{
			component.append(position.getFile().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
		}
		else
		{
			component.append(myCallFrame.getScript().getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
		}

		component.append(":" + (statementStartPosition.getLine() + 1), SimpleTextAttributes.REGULAR_ATTRIBUTES);
		component.setIcon(AllIcons.Debugger.StackFrame);
	}

	@Override
	@Nullable
	public XSourcePosition getSourcePosition()
	{
		TextStreamPosition statementStartPosition = myCallFrame.getStatementStartPosition();

		VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(new File(myCallFrame.getScript().getName()));
		if(file == null)
		{
			return null;
		}
		return XDebuggerUtil.getInstance().createPosition(file, statementStartPosition.getLine());
	}
}
