package consulo.javascript.run.debug.inspect;

import com.github.kklisura.cdt.protocol.events.debugger.Paused;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 31/01/2021
 */
public class InspectSuspendContext extends XSuspendContext
{
	private InspectExecutionStack myInspectExecutionStack;

	public InspectSuspendContext(Paused paused)
	{
		myInspectExecutionStack = new InspectExecutionStack(paused);
	}

	@Nullable
	@Override
	public XExecutionStack getActiveExecutionStack()
	{
		return myInspectExecutionStack;
	}
}
