package org.mustbe.consulo.nodejs.run.debug;

import org.chromium.sdk.DebugContext;
import org.chromium.sdk.DebugEventListener;
import org.chromium.sdk.Script;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class V8DebugEventListener implements DebugEventListener
{
	private final V8DebugProcess myV8DebugProcess;

	public V8DebugEventListener(V8DebugProcess v8DebugProcess)
	{
		myV8DebugProcess = v8DebugProcess;
	}

	@Override
	public void suspended(DebugContext debugContext)
	{
		myV8DebugProcess.setCurrentDebugContext(debugContext);
		myV8DebugProcess.getSession().positionReached(new V8SuspendContext(debugContext));
	}

	@Override
	public void resumed()
	{
		myV8DebugProcess.setCurrentDebugContext(null);
	}

	@Override
	public void disconnected()
	{
		myV8DebugProcess.setCurrentDebugContext(null);
	}

	@Override
	public void scriptLoaded(Script script)
	{
		String name = script.getName();
		if(name == null)
		{
			return;
		}
		myV8DebugProcess.addScript(script);
	}

	@Override
	public void scriptCollected(Script script)
	{

	}

	@Override
	public VmStatusListener getVmStatusListener()
	{
		return null;
	}

	@Override
	public void scriptContentChanged(Script script)
	{

	}

}
