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

package org.mustbe.consulo.nodejs.packages.jom;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.json.jom.JomFileDescriptor;
import org.mustbe.consulo.nodejs.NodeJSIcons;
import org.mustbe.consulo.nodejs.module.extension.NodeJSModuleExtension;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 04.12.2015
 */
public class PackagesFileDescriptor extends JomFileDescriptor<PackagesRootElement>
{
	private static final String PACKAGES_JSON = "package.json";

	public PackagesFileDescriptor()
	{
		super(PackagesRootElement.class);
	}

	@NotNull
	@Override
	public Icon getIcon()
	{
		return NodeJSIcons.Npm;
	}

	@Override
	@RequiredReadAction
	public boolean isMyFile(@NotNull PsiFile psiFile)
	{
		if(!Comparing.equal(PACKAGES_JSON, psiFile.getName()))
		{
			return false;
		}
		NodeJSModuleExtension extension = ModuleUtilCore.getExtension(psiFile, NodeJSModuleExtension.class);
		if(extension == null)
		{
			return false;
		}
		VirtualFile moduleDir = extension.getModule().getModuleDir();
		// if module dir is null or file is not in module dir
		if(moduleDir == null || psiFile.getParent() == null || !moduleDir.equals(psiFile.getParent().getVirtualFile()))
		{
			return false;
		}
		return true;
	}
}