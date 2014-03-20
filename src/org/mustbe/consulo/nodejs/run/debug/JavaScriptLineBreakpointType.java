package org.mustbe.consulo.nodejs.run.debug;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XLineBreakpointTypeBase;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;

/**
 * @author VISTALL
 * @since 20.03.14
 */
public class JavaScriptLineBreakpointType extends XLineBreakpointTypeBase
{
	public JavaScriptLineBreakpointType()
	{
		super("javascript", "JavaScript Breakpoints", new XDebuggerEditorsProvider()
		{
			@NotNull
			@Override
			public FileType getFileType()
			{
				return JavaScriptFileType.INSTANCE;
			}

			@NotNull
			@Override
			public Document createDocument(@NotNull Project project, @NotNull String s, @Nullable XSourcePosition xSourcePosition, @NotNull
			EvaluationMode evaluationMode)
			{
				return null;
			}
		});
	}

	@Override
	public boolean canPutAt(@NotNull VirtualFile file, int line, @NotNull Project project)
	{
		return file.getFileType() == JavaScriptFileType.INSTANCE;
	}
}
