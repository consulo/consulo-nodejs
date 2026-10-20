/*
 * Copyright 2013-2026 consulo.io
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
package consulo.nodejs.impl.ignore;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import jakarta.inject.Inject;

/**
 * @author VISTALL
 * @since 2026-10-20
 */
@ExtensionImpl
public class NodeJSIgnoreFileTypeFactory extends FileTypeFactory {
    private final FileNameMatcherFactory myFileNameMatcherFactory;

    @Inject
    public NodeJSIgnoreFileTypeFactory(FileNameMatcherFactory fileNameMatcherFactory) {
        myFileNameMatcherFactory = fileNameMatcherFactory;
    }

    @Override
    public void createFileTypes(FileTypeConsumer consumer) {
        consumer.consume(
            NpmIgnoreFileType.INSTANCE,
            myFileNameMatcherFactory.createExactFileNameMatcher(NpmIgnoreLanguage.INSTANCE.getFilename())
        );
        consumer.consume(NpmIgnoreFileType.INSTANCE, NpmIgnoreLanguage.INSTANCE.getExtension());
        consumer.consume(
            NodemonIgnoreFileType.INSTANCE,
            myFileNameMatcherFactory.createExactFileNameMatcher(NodemonIgnoreLanguage.INSTANCE.getFilename())
        );
        consumer.consume(NodemonIgnoreFileType.INSTANCE, NodemonIgnoreLanguage.INSTANCE.getExtension());
    }
}
