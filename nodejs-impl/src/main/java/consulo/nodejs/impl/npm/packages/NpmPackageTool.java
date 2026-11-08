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
package consulo.nodejs.impl.npm.packages;

import consulo.nodejs.impl.npm.NpmUtil;
import consulo.repository.ui.PackageInstallScope;
import org.jspecify.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The tool which manages the dependencies of a package - the one its lock file belongs to.
 */
public enum NpmPackageTool {
    NPM(NpmUtil.DEFAULT_EXECUTABLE, "install", "uninstall", "--save-prod", "--save-dev", "--save-peer", "--save-optional"),
    YARN("yarn", "add", "remove", null, "--dev", "--peer", "--optional"),
    PNPM("pnpm", "add", "remove", "--save-prod", "--save-dev", "--save-peer", "--save-optional");

    private final String myExecutable;
    private final String myAddCommand;
    private final String myRemoveCommand;
    private final @Nullable String myProdFlag;
    private final String myDevFlag;
    private final String myPeerFlag;
    private final String myOptionalFlag;

    NpmPackageTool(String executable, String addCommand, String removeCommand, @Nullable String prodFlag, String devFlag, String peerFlag, String optionalFlag) {
        myExecutable = executable;
        myAddCommand = addCommand;
        myRemoveCommand = removeCommand;
        myProdFlag = prodFlag;
        myDevFlag = devFlag;
        myPeerFlag = peerFlag;
        myOptionalFlag = optionalFlag;
    }

    public static NpmPackageTool detect(Path packageDirectory) {
        if (Files.exists(packageDirectory.resolve("pnpm-lock.yaml"))) {
            return PNPM;
        }
        if (Files.exists(packageDirectory.resolve("yarn.lock"))) {
            return YARN;
        }
        return NPM;
    }

    public String getExecutable() {
        return myExecutable;
    }

    /**
     * {@code add <name>@<version> --dev <options>}
     */
    public List<String> add(String packageSpec, @Nullable PackageInstallScope scope, @Nullable String extraOptions) {
        List<String> arguments = new ArrayList<>();
        arguments.add(myAddCommand);
        arguments.add(packageSpec);
        String flag = scopeFlag(scope);
        if (flag != null) {
            arguments.add(flag);
        }
        addOptions(arguments, extraOptions);
        return arguments;
    }

    public List<String> remove(List<String> packageNames) {
        List<String> arguments = new ArrayList<>();
        arguments.add(myRemoveCommand);
        arguments.addAll(packageNames);
        return arguments;
    }

    private @Nullable String scopeFlag(@Nullable PackageInstallScope scope) {
        if (scope == null) {
            return myProdFlag;
        }
        return switch (scope.id()) {
            case NpmScopes.DEV -> myDevFlag;
            case NpmScopes.PEER -> myPeerFlag;
            case NpmScopes.OPTIONAL -> myOptionalFlag;
            default -> myProdFlag;
        };
    }

    private static void addOptions(List<String> arguments, @Nullable String extraOptions) {
        if (extraOptions == null) {
            return;
        }
        for (String option : extraOptions.trim().split("\\s+")) {
            if (!option.isEmpty()) {
                arguments.add(option);
            }
        }
    }
}
