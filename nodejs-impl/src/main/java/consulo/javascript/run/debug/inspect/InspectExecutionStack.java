package consulo.javascript.run.debug.inspect;

import com.github.kklisura.cdt.protocol.events.debugger.Paused;
import com.github.kklisura.cdt.protocol.types.debugger.CallFrame;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 31/01/2021
 */
public class InspectExecutionStack extends XExecutionStack
{
	private List<InspectStackFrame> myStackFrames = new ArrayList<>();

	public InspectExecutionStack(Paused paused)
	{
		super("");

		for(CallFrame callFrame : paused.getCallFrames())
		{
			myStackFrames.add(new InspectStackFrame(callFrame));
		}
	}

	@Nullable
	@Override
	public XStackFrame getTopFrame()
	{
		return myStackFrames.isEmpty() ? null : myStackFrames.get(0);
	}

	@Override
	public void computeStackFrames(XStackFrameContainer container)
	{
		container.addStackFrames(myStackFrames, true);
	}
}
