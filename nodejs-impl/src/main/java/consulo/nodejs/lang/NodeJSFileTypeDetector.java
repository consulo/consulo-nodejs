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

package consulo.nodejs.lang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.io.ByteSequence;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 18.12.2015
 */
public class NodeJSFileTypeDetector implements FileTypeRegistry.FileTypeDetector
{
	@Nullable
	@Override
	public FileType detect(@Nonnull VirtualFile file, @Nonnull ByteSequence byteSequence, @Nullable CharSequence charSequence)
	{
		if(FileUtil.isHashBangLine(charSequence, "/usr/bin/env node"))
		{
			return JavaScriptFileType.INSTANCE;
		}
		return null;
	}

	@Override
	public int getVersion()
	{
		return 2;
	}
}
