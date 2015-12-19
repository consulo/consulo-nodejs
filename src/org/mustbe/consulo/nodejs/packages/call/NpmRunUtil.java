/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.nodejs.packages.call;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.nodejs.NodeJSConstants;
import org.mustbe.consulo.nodejs.module.extension.NodeJSModuleExtension;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.RunContentExecutor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 05.12.2015
 */
public class NpmRunUtil
{
	public static final String UPDATE = "update";

	@Nullable
	public static VirtualFile findNpmModule(@NotNull Module module, @NotNull String moduleName)
	{
		VirtualFile moduleDir = module.getModuleDir();
		if(moduleDir != null)
		{
			VirtualFile npmModuleDir = moduleDir.findFileByRelativePath(NodeJSConstants.NODE_MODULES + "/" + moduleName);
			if(npmModuleDir != null)
			{
				return npmModuleDir;
			}
		}

		Sdk sdk = ModuleUtilCore.getSdk(module, NodeJSModuleExtension.class);
		if(sdk == null)
		{
			return null;
		}

		VirtualFile homeDirectory = sdk.getHomeDirectory();
		if(homeDirectory == null)
		{
			return null;
		}
		VirtualFile moduleByBinDirectory = homeDirectory.findChild("bin/" + NodeJSConstants.NODE_MODULES + "/" + moduleName);
		if(moduleByBinDirectory != null)
		{
			return moduleByBinDirectory;
		}
		return homeDirectory.findFileByRelativePath(NodeJSConstants.NODE_MODULES + "/" + moduleName);
	}

	@NotNull
	public static File getNpmExecutable(@NotNull String home)
	{
		String executable = SystemInfo.isWindows ? "npm.cmd" : "npm";

		File firstTry = new File(home, "bin/" + executable);
		if(firstTry.exists())
		{
			return firstTry;
		}
		return new File(home, executable);
	}

	@RequiredDispatchThread
	public static void run(@NotNull Project project, VirtualFile packageFile, @NotNull String action)
	{
		NodeJSModuleExtension extension = ModuleUtilCore.getExtension(project, packageFile, NodeJSModuleExtension.class);
		if(extension == null)
		{
			return;
		}

		Sdk sdk = extension.getSdk();
		if(sdk == null)
		{
			Messages.showErrorDialog(project, "NodeJS bundle is undefined", "Error");
			return;
		}

		File npmExecutable = getNpmExecutable(sdk.getHomePath());
		if(!npmExecutable.exists())
		{
			Messages.showErrorDialog(project, "'npm' executable is not found", "Error");
			return;
		}
		GeneralCommandLine commandLine = new GeneralCommandLine();
		commandLine.setExePath(npmExecutable.getPath());
		commandLine.addParameter(action);
		commandLine.setWorkDirectory(packageFile.getParent().getPath());

		final RunContentExecutor contentExecutor;
		try
		{
			OSProcessHandler process = new OSProcessHandler(commandLine);
			ProcessTerminatedListener.attach(process);
			contentExecutor = new RunContentExecutor(project, process).withTitle("npm " + action).withActivateToolWindow(false);
			contentExecutor.run();
		}
		catch(ExecutionException e)
		{
			Messages.showErrorDialog(project, "Fail to run npm manager: " + e.getMessage(), "Error");
		}
	}
}
