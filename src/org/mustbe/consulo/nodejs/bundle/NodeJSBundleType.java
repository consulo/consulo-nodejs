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

package org.mustbe.consulo.nodejs.bundle;

import java.io.File;
import java.util.Arrays;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.nodejs.NodeJSIcons;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;

/**
 * @author VISTALL
 * @since 14.03.14
 */
public class NodeJSBundleType extends SdkType
{
	@NotNull
	public static String getExePath(@NotNull Sdk sdk)
	{
		String homePath = sdk.getHomePath();
		assert homePath != null;
		return getExePath(homePath);
	}

	@NotNull
	public static String getExePath(@NotNull String home)
	{
		return home + "/" + (SystemInfo.isWindows ? "node.exe" : "node");
	}

	public NodeJSBundleType()
	{
		super("NODEJS");
	}

	@Override
	public boolean isValidSdkHome(String s)
	{
		return new File(getExePath(s)).exists();
	}

	@Nullable
	@Override
	public String getVersionString(String s)
	{
		try
		{
			ProcessOutput processOutput = ExecUtil.execAndGetOutput(Arrays.asList(getExePath(s), "-v"), s);
			String stdout = processOutput.getStdout();
			if(StringUtil.startsWith(stdout, "v"))
			{
				stdout = stdout.substring(1, stdout.length());
			}
			return stdout;
		}
		catch(ExecutionException e)
		{
			return "unknown";
		}
	}

	@Override
	public String suggestSdkName(String s, String s2)
	{
		return "nodejs";
	}

	@NotNull
	@Override
	public String getPresentableName()
	{
		return "NodeJS";
	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return NodeJSIcons.NodeJS;
	}
}
