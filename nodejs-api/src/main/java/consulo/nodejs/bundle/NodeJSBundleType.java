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

package consulo.nodejs.bundle;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SystemProperties;
import consulo.container.plugin.PluginManager;
import consulo.nodejs.icon.NodeJSApiIconGroup;
import consulo.platform.Platform;
import consulo.roots.types.BinariesOrderRootType;
import consulo.roots.types.SourcesOrderRootType;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 14.03.14
 */
public class NodeJSBundleType extends SdkType
{
	@Nonnull
	public static NodeJSBundleType getInstance()
	{
		return EP_NAME.findExtensionOrFail(NodeJSBundleType.class);
	}

	@Nonnull
	public static File getExePath(@Nonnull Sdk sdk)
	{
		String homePath = sdk.getHomePath();
		assert homePath != null;
		return getExePath(homePath);
	}

	@Nonnull
	public static File getExePath(@Nonnull String home)
	{
		return getExePath(home, "node.exe", "node");
	}

	@Nonnull
	public static File getExePath(@Nonnull String home, @Nonnull String winName, String otherName)
	{
		String executable = Platform.current().os().isWindows() ? winName : otherName;

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

	@Nonnull
	@Override
	public Collection<String> suggestHomePaths()
	{
		if(Platform.current().os().isWindows())
		{
			return List.of();
		}
		else
		{
			List<String> paths = new ArrayList<>();
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
	public void setupSdkPaths(Sdk sdk)
	{
		SdkModificator modificator = sdk.getSdkModificator();

		VirtualFile stubsDirectory = LocalFileSystem.getInstance().findFileByIoFile(new File(PluginManager.getPluginPath(NodeJSBundleType.class), "stubs"));
		if(stubsDirectory != null)
		{
			for(VirtualFile file : stubsDirectory.getChildren())
			{
				if(file.getFileType() == JavaScriptFileType.INSTANCE)
				{
					modificator.addRoot(file, BinariesOrderRootType.getInstance());
					modificator.addRoot(file, SourcesOrderRootType.getInstance());
				}
			}
		}

		modificator.commitChanges();
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
	public boolean isRootTypeApplicable(OrderRootType type)
	{
		return type == BinariesOrderRootType.getInstance();
	}

	@Override
	public String suggestSdkName(String s, String sdkHome)
	{
		return "nodejs " + getVersionString(sdkHome);
	}

	@Nonnull
	@Override
	public String getPresentableName()
	{
		return "NodeJS";
	}

	@Nullable
	@Override
	public Image getIcon()
	{
		return NodeJSApiIconGroup.nodejs();
	}
}
