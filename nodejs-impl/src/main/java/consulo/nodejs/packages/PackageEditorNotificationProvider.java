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

package consulo.nodejs.packages;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.nodejs.packages.call.NpmRunUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.editor.notifications.EditorNotificationProvider;
import consulo.json.JsonFileType;
import consulo.json.jom.JomElement;
import consulo.json.jom.JomFileElement;
import consulo.json.jom.JomManager;

/**
 * @author VISTALL
 * @since 05.12.2015
 */
public class PackageEditorNotificationProvider implements EditorNotificationProvider<EditorNotificationPanel>
{
	private static final Key<EditorNotificationPanel> ourKey = Key.create("PackageEditorNotificationProvider");

	private Project myProject;

	public PackageEditorNotificationProvider(Project project)
	{
		myProject = project;
	}

	@Nonnull
	@Override
	public Key<EditorNotificationPanel> getKey()
	{
		return ourKey;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public EditorNotificationPanel createNotificationPanel(@Nonnull final VirtualFile file, @Nonnull FileEditor fileEditor)
	{
		if(file.getFileType() != JsonFileType.INSTANCE)
		{
			return null;
		}

		final PsiFile jsonFile = PsiManager.getInstance(myProject).findFile(file);
		if(jsonFile == null)
		{
			return null;
		}
		JomFileElement<JomElement> fileElement = JomManager.getInstance(myProject).getFileElement(jsonFile);
		if(fileElement == null)
		{
			return null;
		}
		EditorNotificationPanel panel = new EditorNotificationPanel();
		panel.text("'npm' package manager");

		panel.createActionLabel("'update'", new Runnable()
		{
			@Override
			@RequiredDispatchThread
			public void run()
			{
				NpmRunUtil.run(myProject, file, NpmRunUtil.UPDATE);
			}
		});
		return panel;
	}
}
