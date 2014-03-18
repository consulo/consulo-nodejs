package org.mustbe.consulo.nodejs.bundle;

import java.io.File;
import java.util.Arrays;

import javax.swing.Icon;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.nodejs.NodeJSIcons;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.execution.util.ExecUtil;
import com.intellij.openapi.projectRoots.AdditionalDataConfigurable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkAdditionalData;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
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

	@Nullable
	@Override
	public String suggestHomePath()
	{
		return null;
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

	@Nullable
	@Override
	public AdditionalDataConfigurable createAdditionalDataConfigurable(SdkModel sdkModel, SdkModificator sdkModificator)
	{
		return null;
	}

	@NotNull
	@Override
	public String getPresentableName()
	{
		return "NodeJS";
	}

	@Override
	public void saveAdditionalData(SdkAdditionalData sdkAdditionalData, Element element)
	{

	}

	@Nullable
	@Override
	public Icon getIcon()
	{
		return NodeJSIcons.NodeJS;
	}
}
