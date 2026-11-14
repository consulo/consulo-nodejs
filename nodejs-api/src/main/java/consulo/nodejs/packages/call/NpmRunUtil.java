/*
 * Copyright 2013-2017 consulo.io
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

package consulo.nodejs.packages.call;

import consulo.content.bundle.Sdk;
import consulo.execution.RunContentExecutor;
import consulo.execution.process.ProcessTerminatedListener;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.nodejs.NodeJSConstants;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.nodejs.module.extension.NodeJSModuleExtension;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.cmd.GeneralCommandLine;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awt.Messages;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 05.12.2015
 */
public class NpmRunUtil
{
	public static final String UPDATE = "update";

	@Nullable
	public static VirtualFile findNpmModule(@Nonnull Module module, @Nonnull String moduleName)
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

	/**
	 * The npm of the bundle - a path of the machine of the bundle
	 */
	@Nonnull
	public static Path getNpmExecutable(@Nonnull Sdk sdk)
	{
		return NodeJSBundleType.getExePath(sdk.getPlatform(), sdk.getHomeNioPath(), "npm.cmd", "npm");
	}

	@RequiredUIAccess
	public static void run(@Nonnull Project project, VirtualFile packageFile, @Nonnull String action)
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

		Path npmExecutable = getNpmExecutable(sdk);
		if(!Files.exists(npmExecutable))
		{
			Messages.showErrorDialog(project, "'npm' executable is not found", "Error");
			return;
		}
		GeneralCommandLine commandLine = new GeneralCommandLine();
		commandLine.withPlatform(sdk.getPlatform());
		commandLine.withExecutablePath(npmExecutable);
		commandLine.addParameter(action);
		commandLine.withWorkingDirectory(packageFile.getParent().toNioPath());

		final RunContentExecutor contentExecutor;
		try
		{
			ProcessHandler process = ProcessHandlerBuilder.create(commandLine).build();
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
