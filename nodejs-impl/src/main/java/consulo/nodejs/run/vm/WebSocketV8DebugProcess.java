package consulo.nodejs.run.vm;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.xdebugger.XDebugSession;
import consulo.javascript.run.debug.V8BaseDebugProcess;
import consulo.javascript.run.debug.V8DebugEventListener;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author VISTALL
 * @since 2020-06-16
 */
public class WebSocketV8DebugProcess extends V8BaseDebugProcess<WebSocketVmImpl>
{
	public WebSocketV8DebugProcess(@Nonnull XDebugSession session, ExecutionResult result, int port) throws ExecutionException
	{
		super(session, result);
	}

	public void attach(String url) throws IOException
	{
		myVm = new WebSocketVmImpl(url);

		myVm.attach(new V8DebugEventListener(this));
	}
}
