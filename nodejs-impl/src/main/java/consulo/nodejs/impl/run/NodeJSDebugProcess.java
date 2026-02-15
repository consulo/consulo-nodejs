package consulo.nodejs.impl.run;

import com.github.kklisura.cdt.protocol.events.debugger.Paused;
import consulo.execution.ExecutionResult;
import consulo.execution.debug.XDebugSession;
import consulo.javascript.debugger.cdt.CDTProcessBase;
import consulo.process.ExecutionException;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2026-02-15
 */
public class NodeJSDebugProcess extends CDTProcessBase {
    private boolean myFirstPause = true;

    public NodeJSDebugProcess(@Nonnull XDebugSession session, ExecutionResult result) throws ExecutionException {
        super(session, result);
    }

    @Override
    protected void onPause(Paused event) {
        if (myFirstPause) {
            myFirstPause = false;
            invoke(devTools -> devTools.getDebugger().resume());
            return;
        }

        super.onPause(event);
    }
}
