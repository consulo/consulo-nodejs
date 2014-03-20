package org.mustbe.consulo.nodejs.run.debug;

import org.chromium.sdk.DebugContext;
import org.jetbrains.annotations.Nullable;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class V8SuspendContext extends XSuspendContext
{
	private final DebugContext myDebugContext;
	private V8ExecutionStack myStack;

	public V8SuspendContext(DebugContext debugContext)
	{
		myDebugContext = debugContext;
		myStack = new V8ExecutionStack(debugContext);
	}

	@Nullable
	@Override
	public XExecutionStack getActiveExecutionStack()
	{
		return myStack;
	}

	@Override
	public XExecutionStack[] getExecutionStacks()
	{
		return new XExecutionStack[] {myStack};
	}

	public DebugContext getDebugContext()
	{
		return myDebugContext;
	}
}
