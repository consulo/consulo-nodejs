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
package consulo.nodejs.it;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.content.base.BinariesOrderRootType;
import consulo.content.base.SourcesOrderRootType;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.content.bundle.SdkTable;
import consulo.content.library.Library;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.javascript.library.JavaScriptStandardLibrary;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.orderEntry.LibraryOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.nodejs.bundle.NodeJSBundleType;
import consulo.nodejs.npm.NpmConstants;
import consulo.nodejs.npm.NpmPackage;
import consulo.nodejs.npm.NpmPackageParser;
import consulo.nodejs.npm.NpmProjectModel;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The standard library of JavaScript for Node.js: from the SDK, once.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class NpmStandardLibraryTest {
    /**
     * A Node.js bundle has the bundled lib directory as root; the lib directory of an older plugin is replaced
     */
    @Test
    public void nodeBundleHasStandardLibrary(HeadlessProjects projects) throws Exception {
        VirtualFile libDirectory = JavaScriptStandardLibrary.getInstance().getLibDirectory();
        assertThat(libDirectory).isNotNull();

        Path old = projects.newDirectory();
        Files.writeString(old.resolve(JavaScriptStandardLibrary.ES5_LIB_FILE), "");
        VirtualFile oldLibDirectory = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(old);
        assertThat(oldLibDirectory).isNotNull();

        SdkTable sdkTable = SdkTable.getInstance();
        Sdk sdk = WriteAction.compute(() -> {
            Sdk created = sdkTable.createSdk("node-test", NodeJSBundleType.getInstance());
            SdkModificator modificator = created.getSdkModificator();
            modificator.addRoot(oldLibDirectory, BinariesOrderRootType.ID);
            modificator.addRoot(oldLibDirectory, SourcesOrderRootType.ID);
            modificator.commitChanges();
            sdkTable.addSdk(created);
            return created;
        });
        try {
            WriteAction.run(() -> {
                SdkModificator modificator = sdk.getSdkModificator();
                assertThat(NodeJSBundleType.setupStandardLibrary(modificator)).isTrue();
                modificator.commitChanges();
            });
            assertThat(sdk.getRootProvider().getFiles(BinariesOrderRootType.ID)).containsExactly(libDirectory);
            assertThat(sdk.getRootProvider().getFiles(SourcesOrderRootType.ID)).containsExactly(libDirectory);

            // up to date: nothing to change
            WriteAction.run(() -> {
                SdkModificator modificator = sdk.getSdkModificator();
                assertThat(NodeJSBundleType.setupStandardLibrary(modificator)).isFalse();
                modificator.commitChanges();
            });
        }
        finally {
            WriteAction.run(() -> sdkTable.removeSdk(sdk));
        }
    }

    /**
     * The lib.*.d.ts of an installed typescript are not a second standard library: excluded from its npm library
     */
    @Test
    public void typescriptLibIsExcluded(HeadlessProjects projects) throws Exception {
        Path directory = projects.newDirectory();
        Files.writeString(directory.resolve(NpmConstants.PACKAGE_JSON), "{\"name\": \"tool\", \"devDependencies\": {\"typescript\": \"^5.9.0\"}}");
        Files.createDirectories(directory.resolve("node_modules/typescript/lib"));
        Files.writeString(directory.resolve("node_modules/typescript/lib/" + JavaScriptStandardLibrary.ES5_LIB_FILE), "");
        Files.writeString(directory.resolve("node_modules/typescript/lib/typescript.js"), "");
        Files.writeString(directory.resolve("node_modules/typescript/package.json"), "{\"name\": \"typescript\"}");
        VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory);
        assertThat(root).isNotNull();
        root.refresh(false, true);
        // the lib directory is recognized by its lib.es5.d.ts: in the VFS before the npm library is set up
        assertThat(LocalFileSystem.getInstance().refreshAndFindFileByNioFile(
            directory.resolve("node_modules/typescript/lib/" + JavaScriptStandardLibrary.ES5_LIB_FILE))).isNotNull();

        Project project = projects.open(directory);
        Module module = WriteAction.compute(() -> {
            consulo.module.ModifiableModuleModel moduleModel = ModuleManager.getInstance(project).getModifiableModel();
            Module created = moduleModel.newModule("tool", directory.toString());
            moduleModel.commit();
            ModifiableRootModel model = ModuleRootManager.getInstance(created).getModifiableModel();
            model.addContentEntry(root);
            model.commit();
            return created;
        });
        NpmPackage npmPackage = NpmPackageParser.parse(directory.resolve(NpmConstants.PACKAGE_JSON));
        assertThat(npmPackage).isNotNull();
        WriteAction.run(() -> {
            ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
            NpmProjectModel.getInstance(project).apply(model, root, npmPackage);
            model.commit();
        });
        HeadlessProjects.awaitIdle(project);

        ReadAction.run(() -> {
            Library typescript = null;
            for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
                if (orderEntry instanceof LibraryOrderEntry libraryOrderEntry && "npm: typescript".equals(libraryOrderEntry.getLibraryName())) {
                    typescript = libraryOrderEntry.getLibrary();
                }
            }
            assertThat(typescript).isNotNull();
            assertThat(Arrays.asList(typescript.getExcludedRootUrls())).containsExactly(root.getUrl() + "/node_modules/typescript/lib");
        });
    }
}
