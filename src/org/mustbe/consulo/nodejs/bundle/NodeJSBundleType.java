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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.nodejs.NodeJSIcons;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SystemProperties;

/**
 * @author VISTALL
 * @since 14.03.14
 */
public class NodeJSBundleType extends SdkType
{
	@NotNull
	@LazyInstance
	public static NodeJSBundleType getInstance()
	{
		return EP_NAME.findExtension(NodeJSBundleType.class);
	}

	@NotNull
	public static File getExePath(@NotNull Sdk sdk)
	{
		String homePath = sdk.getHomePath();
		assert homePath != null;
		return getExePath(homePath);
	}

	@NotNull
	public static File getExePath(@NotNull String home)
	{
		String executable = SystemInfo.isWindows ? "node.exe" : "node";

		File firstTry = new File(home, "bin/" + executable);
		if(firstTry.exists())
		{
			return firstTry;
		}
		return new File(home, executable);
	}

	public NodeJSBundleType()
	{
		super("NODEJS");
	}

	@NotNull
	@Override
	public Collection<String> suggestHomePaths()
	{
		if(SystemInfo.isWindows)
		{
			return Collections.emptyList();
		}
		else
		{
			List<String> paths = new ArrayList<String>();
			String userHome = SystemProperties.getUserHome();

			File nvmHome = new File(userHome, ".nvm/versions/node");
			if(nvmHome.exists())
			{
				for(File file : nvmHome.listFiles())
				{
					if(file.isDirectory())
					{
						paths.add(file.getPath());
					}
				}
			}
			return paths;
		}
	}

	@Override
	public boolean canCreatePredefinedSdks()
	{
		return true;
	}

	@Override
	public boolean isValidSdkHome(String sdkHome)
	{
		return getExePath(sdkHome).exists();
	}

	@Nullable
	@Override
	public String getVersionString(String s)
	{
		try
		{
			GeneralCommandLine commandLine = new GeneralCommandLine();
			commandLine.setExePath(getExePath(s).getPath());
			commandLine.withWorkDirectory(s);
			commandLine.addParameter("-v");

			ProcessOutput processOutput = ExecUtil.execAndGetOutput(commandLine);
			String stdout = processOutput.getStdout();
			if(StringUtil.startsWith(stdout, "v"))
			{
				stdout = stdout.substring(1, stdout.length());
			}
			return stdout.trim();
		}
		catch(ExecutionException e)
		{
			return null;
		}
	}

	@Override
	public String suggestSdkName(String s, String sdkHome)
	{
		return "nodejs " + getVersionString(sdkHome);
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
