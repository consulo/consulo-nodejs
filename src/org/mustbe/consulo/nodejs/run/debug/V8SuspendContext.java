/*
 * Copyright 2013-2014 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
