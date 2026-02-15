package consulo.nodejs.impl.run;

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
    public NodeJSDebugProcess(@Nonnull XDebugSession session, ExecutionResult result) throws ExecutionException {
        super(session, result);
    }
}
