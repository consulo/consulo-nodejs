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
package consulo.nodejs.npm;

import consulo.nodejs.NodeJSConstants;

import java.util.List;

/**
 * @since 2026-10-31
 */
public interface NpmConstants {
    String PACKAGE_JSON = "package.json";
    String PACKAGE_LOCK = "package-lock.json";
    String NODE_MODULES = NodeJSConstants.NODE_MODULES;

    /**
     * Files whose change requires the package to be imported again
     */
    List<String> AUTO_IMPORT_FILES = List.of(PACKAGE_JSON, PACKAGE_LOCK, "yarn.lock", "pnpm-lock.yaml");

    /**
     * The prefix of the module libraries of the npm dependencies ({@code npm: vue}); only these libraries are changed by
     * the npm model, the other order entries of a module (Maven, the user) are kept
     */
    String LIBRARY_PREFIX = "npm: ";

    /**
     * The directories of an installed package excluded from its library: tests, documentation and samples are never
     * imported by the code of the module
     */
    List<String> PACKAGE_EXCLUDED_DIRECTORIES = List.of(
        "test",
        "tests",
        "__tests__",
        "spec",
        "docs",
        "doc",
        "example",
        "examples",
        "benchmark",
        "coverage",
        ".github"
    );

    String TASK_INSTALL = "install";
    String TASK_CI = "ci";
    String TASK_UPDATE = "update";
    List<String> COMMAND_TASKS = List.of(TASK_INSTALL, TASK_CI, TASK_UPDATE);
}
