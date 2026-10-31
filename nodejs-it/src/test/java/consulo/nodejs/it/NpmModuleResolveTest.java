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

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListenerAdapter;
import consulo.externalSystem.model.task.ExternalSystemTaskType;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.javascript.module.resolve.JavaScriptModuleRequest.Kind;
import consulo.javascript.module.resolve.JavaScriptModuleResolution;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiManager;
import consulo.nodejs.impl.npm.NpmSystem;
import consulo.nodejs.impl.npm.project.NpmProjectResolver;
import consulo.project.Project;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The module specifiers of JavaScript and TypeScript resolved to the packages of npm.
 */
@ExtendWith(HeadlessProjectExtension.class)
public class NpmModuleResolveTest {
    @Test
    public void packagesResolveLikeNode(HeadlessProjects projects) throws Exception {
        Path directory = projects.newDirectory();
        write(directory, "package.json", "{\"name\": \"monorepo\", \"workspaces\": [\"packages/*\"]}");
        write(directory, "packages/app/package.json", "{\"name\": \"app\", \"dependencies\": {\"vue\": \"*\", \"@acme/ui\": \"*\"}}");
        write(directory, "packages/app/src/main.js", "import { createApp } from 'vue';\nconst _ = require('lodash');\nimport './util';\n");
        write(directory, "packages/app/src/util.js", "export const x = 1;\n");
        // a workspace, not installed yet: no node_modules/@acme/ui
        write(directory, "packages/ui/package.json", "{\"name\": \"@acme/ui\", \"main\": \"lib/index.js\", \"types\": \"lib/index.d.ts\"}");
        write(directory, "packages/ui/lib/index.js", "export const Button = {};\n");
        write(directory, "packages/ui/lib/index.d.ts", "export declare const Button: object;\n");
        // hoisted to the root
        write(directory, "node_modules/vue/package.json", """
            {
                "name": "vue",
                "main": "index.js",
                "exports": {
                    ".": {
                        "types": "./dist/vue.d.ts",
                        "import": "./dist/vue.mjs",
                        "require": "./dist/vue.cjs"
                    },
                    "./dist/*": "./dist/*",
                    "./package.json": "./package.json"
                }
            }
            """);
        write(directory, "node_modules/vue/dist/vue.d.ts", "");
        write(directory, "node_modules/vue/dist/vue.mjs", "");
        write(directory, "node_modules/vue/dist/vue.cjs", "");
        write(directory, "node_modules/vue/dist/vue.esm-browser.js", "");
        write(directory, "node_modules/lodash/package.json", "{\"name\": \"lodash\", \"main\": \"lodash.js\"}");
        write(directory, "node_modules/lodash/lodash.js", "");
        write(directory, "node_modules/lodash/fp.js", "");
        write(directory, "node_modules/express/package.json", "{\"name\": \"express\"}");
        write(directory, "node_modules/express/index.js", "");
        write(directory, "node_modules/@types/express/index.d.ts", "");
        write(directory, "node_modules/@types/scope__lib/index.d.ts", "");
        VirtualFile root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(directory);
        assertThat(root).isNotNull();
        root.refresh(false, true);

        Project project = projects.open(directory);
        importData(project, resolve(project, directory));
        HeadlessProjects.awaitIdle(project);

        VirtualFile main = LocalFileSystem.getInstance().findFileByNioFile(directory.resolve("packages/app/src/main.js"));
        assertThat(main).isNotNull();
        ReadAction.run(() -> {
            PsiFile context = PsiManager.getInstance(project).findFile(main);
            assertThat(context).isNotNull();

            assertThat(resolved(context, "vue", Kind.IMPORT)).isEqualTo("node_modules/vue/dist/vue.mjs");
            assertThat(resolved(context, "vue", Kind.REQUIRE)).isEqualTo("node_modules/vue/dist/vue.cjs");
            assertThat(resolved(context, "vue", Kind.TYPES)).isEqualTo("node_modules/vue/dist/vue.d.ts");
            assertThat(resolved(context, "vue/dist/vue.esm-browser.js", Kind.IMPORT)).isEqualTo("node_modules/vue/dist/vue.esm-browser.js");
            // not exported
            assertThat(resolved(context, "vue/index.js", Kind.IMPORT)).isNull();

            assertThat(resolved(context, "lodash", Kind.REQUIRE)).isEqualTo("node_modules/lodash/lodash.js");
            assertThat(resolved(context, "lodash/fp", Kind.IMPORT)).isEqualTo("node_modules/lodash/fp.js");

            assertThat(resolved(context, "@acme/ui", Kind.IMPORT)).isEqualTo("packages/ui/lib/index.js");
            assertThat(resolved(context, "@acme/ui", Kind.TYPES)).isEqualTo("packages/ui/lib/index.d.ts");

            assertThat(resolved(context, "express", Kind.TYPES)).isEqualTo("node_modules/express/index.js");
            assertThat(resolved(context, "@scope/lib", Kind.TYPES)).isEqualTo("node_modules/@types/scope__lib/index.d.ts");

            assertThat(resolved(context, "./util", Kind.IMPORT)).isNull();
            assertThat(resolved(context, "node:fs", Kind.IMPORT)).isNull();
            assertThat(resolved(context, "missing", Kind.IMPORT)).isNull();

            // the references of the specifiers: Ctrl+click on 'vue', 'lodash', './util'
            String text = context.getText();
            assertThat(referenced(context, text.indexOf("'vue'") + 2)).isEqualTo("vue.mjs");
            assertThat(referenced(context, text.indexOf("'lodash'") + 2)).isEqualTo("lodash.js");
            assertThat(referenced(context, text.indexOf("'./util'") + 3)).isEqualTo("util.js");
        });
    }

    private static @Nullable String resolved(PsiFile context, String specifier, Kind kind) {
        VirtualFile file = JavaScriptModuleResolution.resolveFile(context, specifier, kind);
        if (file == null) {
            return null;
        }
        VirtualFile root = context.getVirtualFile().getParent().getParent().getParent().getParent();
        return file.getPath().substring(root.getPath().length() + 1);
    }

    private static @Nullable String referenced(PsiFile file, int offset) {
        PsiReference reference = file.findReferenceAt(offset);
        assertThat(reference).as("reference at " + offset).isNotNull();
        PsiElement target = reference.resolve();
        return target instanceof PsiFile targetFile ? targetFile.getName() : null;
    }

    private static void write(Path directory, String relativePath, String content) throws Exception {
        Path file = directory.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    private static DataNode<ProjectData> resolve(Project project, Path directory) {
        ExternalSystemTaskId id = ExternalSystemTaskId.create(NpmSystem.SYSTEM_ID, ExternalSystemTaskType.RESOLVE_PROJECT, project);
        DataNode<ProjectData> projectNode = new NpmProjectResolver().resolveProjectInfo(
            id, directory.toString(), false, null, new ExternalSystemTaskNotificationListenerAdapter() {
            }
        );
        assertThat(projectNode).isNotNull();
        return projectNode;
    }

    private static void importData(Project project, DataNode<ProjectData> projectNode) {
        ProjectDataManager dataManager = Application.get().getInstance(ProjectDataManager.class);
        ExternalSystemApiUtil.executeProjectChangeAction(true, new DisposeAwareProjectChange(project) {
            @Override
            public void execute() {
                dataManager.importData(projectNode.getKey(), Collections.singleton(projectNode), project, true);
            }
        });
    }
}
