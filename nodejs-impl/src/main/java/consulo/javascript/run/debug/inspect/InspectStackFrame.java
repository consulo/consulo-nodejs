package consulo.javascript.run.debug.inspect;

import com.github.kklisura.cdt.protocol.types.debugger.CallFrame;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XStackFrame;
import consulo.javascript.run.debug.V8ScriptUtil;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author VISTALL
 * @since 31/01/2021
 */
public class InspectStackFrame extends XStackFrame
{
	private final CallFrame myCallFrame;

	public InspectStackFrame(CallFrame callFrame)
	{
		myCallFrame = callFrame;
	}

	@Nullable
	@Override
	public Object getEqualityObject()
	{
		return myCallFrame.getCallFrameId();
	}

	@Nullable
	@Override
	public XSourcePosition getSourcePosition()
	{
		//return XDebuggerUtil.getInstance().createPosition(InspectDebugProcess.toVirtualFile(myCallFrame.getScript(), true), statementStartPosition.getLine());


		String url = myCallFrame.getUrl();
		if(url != null && url.startsWith("file://"))
		{
			try
			{
				Path path = Paths.get(new URI(url));

				VirtualFile file = LocalFileSystem.getInstance().findFileByIoFile(path.toFile());

				if(file != null)
				{

					return XDebuggerUtil.getInstance().createPosition(file, myCallFrame.getLocation().getLineNumber(), myCallFrame.getLocation().getColumnNumber());
				}
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
		VirtualFile file = InspectDebugProcess.createLightFile(url);
		return XDebuggerUtil.getInstance().createPosition(file, myCallFrame.getLocation().getLineNumber(), myCallFrame.getLocation().getColumnNumber());
	}
}
