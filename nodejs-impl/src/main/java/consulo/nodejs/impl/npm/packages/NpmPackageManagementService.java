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

import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.http.HttpRequests;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.nodejs.impl.npm.NpmUtil;
import consulo.nodejs.impl.npm.setting.NpmExecutionSettings;
import consulo.nodejs.npm.NpmPackage;
import consulo.nodejs.npm.NpmPackageParser;
import consulo.nodejs.npm.NpmProjectModel;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.util.CapturingProcessUtil;
import consulo.process.util.ProcessOutput;
import consulo.repository.ui.InstalledPackage;
import consulo.repository.ui.PackageInstallScope;
import consulo.repository.ui.PackageManagementServiceEx;
import consulo.repository.ui.RepoPackage;
import consulo.repository.ui.SearchablePackageManagementService;
import consulo.util.concurrent.AsyncResult;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The dependencies of the package.json of a module, from the npm registry, changed by the tool of the package - npm, yarn or pnpm.
 */
public class NpmPackageManagementService extends PackageManagementServiceEx implements SearchablePackageManagementService {
    private static final String REGISTRY = "https://registry.npmjs.org/";
    // the abbreviated metadata of a package: its versions and dist-tags, without the readme of each version
    private static final String ABBREVIATED_METADATA = "application/vnd.npm.install-v1+json";

    private static final class NpmInstalledPackage extends InstalledPackage {
        private final PackageInstallScope myScope;

        private NpmInstalledPackage(String name, @Nullable String version, PackageInstallScope scope) {
            super(name, version);
            myScope = scope;
        }

        @Override
        public @Nullable String getTooltipText() {
            return myScope.name().get();
        }
    }

    private final Module myModule;
    private final VirtualFile myPackageJson;
    private final Map<String, String> myDescriptions = new ConcurrentHashMap<>();

    public NpmPackageManagementService(Module module, VirtualFile packageJson) {
        myModule = module;
        myPackageJson = packageJson;
    }

    private Path getPackageDirectory() {
        return myPackageJson.getParent().toNioPath();
    }

    @Override
    public List<PackageInstallScope> getInstallScopes() {
        return NpmScopes.ALL;
    }

    @Override
    public @Nullable PackageInstallScope getDefaultInstallScope() {
        return NpmScopes.PROD_SCOPE;
    }

    @Override
    public boolean canUseExtraOptions() {
        return true;
    }

    @Override
    public List<RepoPackage> getAllPackages() throws IOException {
        return List.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RepoPackage> getPackages(String searchQuery, int from, int to) {
        if (StringUtil.isEmptyOrSpaces(searchQuery)) {
            return List.of();
        }
        try {
            String url = REGISTRY + "-/v1/search?text=" + URLEncoder.encode(searchQuery, StandardCharsets.UTF_8) + "&from=" + from + "&size=" + (to - from);
            Object json = NpmPackageParser.readJson(HttpRequests.request(url).productNameAsUserAgent().readString(null));
            List<RepoPackage> packages = new ArrayList<>();
            if (json instanceof Map<?, ?> root && root.get("objects") instanceof List<?> objects) {
                for (Object object : objects) {
                    if (object instanceof Map<?, ?> entry && entry.get("package") instanceof Map<?, ?> pkg && pkg.get("name") instanceof String name) {
                        String version = pkg.get("version") instanceof String v ? v : null;
                        if (pkg.get("description") instanceof String description) {
                            myDescriptions.put(name, description);
                        }
                        packages.add(new RepoPackage(name, REGISTRY, version));
                    }
                }
            }
            return packages;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPageSize() {
        return 50;
    }

    @Override
    public List<? extends InstalledPackage> getInstalledPackagesList() throws IOException {
        NpmPackage npmPackage = ReadAction.compute(() -> myPackageJson.isValid() ? NpmPackageParser.parse(myPackageJson) : null);
        if (npmPackage == null) {
            return List.of();
        }
        Path nodeModules = getPackageDirectory().resolve("node_modules");
        List<InstalledPackage> packages = new ArrayList<>();
        addInstalled(packages, npmPackage.dependencies(), NpmScopes.PROD_SCOPE, nodeModules);
        addInstalled(packages, npmPackage.devDependencies(), NpmScopes.DEV_SCOPE, nodeModules);
        addInstalled(packages, npmPackage.peerDependencies(), NpmScopes.PEER_SCOPE, nodeModules);
        addInstalled(packages, npmPackage.optionalDependencies(), NpmScopes.OPTIONAL_SCOPE, nodeModules);
        return packages;
    }

    private static void addInstalled(List<InstalledPackage> packages, Map<String, String> dependencies, PackageInstallScope scope, Path nodeModules) {
        for (Map.Entry<String, String> entry : dependencies.entrySet()) {
            // the version node_modules has, else the range package.json asks for
            String version = installedVersion(nodeModules.resolve(entry.getKey()).resolve("package.json"));
            packages.add(new NpmInstalledPackage(entry.getKey(), version != null ? version : entry.getValue(), scope));
        }
    }

    private static @Nullable String installedVersion(Path packageJson) {
        if (!Files.isRegularFile(packageJson)) {
            return null;
        }
        NpmPackage npmPackage = NpmPackageParser.parse(packageJson);
        return npmPackage == null ? null : npmPackage.version();
    }

    @Override
    public void installPackage(RepoPackage repoPackage, @Nullable String version, @Nullable PackageInstallScope scope, @Nullable String extraOptions, Listener listener) {
        String name = repoPackage.getName();
        String spec = version == null ? name : name + "@" + version;
        NpmPackageTool tool = NpmPackageTool.detect(getPackageDirectory());
        run(name, tool, tool.add(spec, scope, extraOptions), listener);
    }

    @Override
    public void installPackage(RepoPackage repoPackage, @Nullable String version, boolean forceUpgrade, @Nullable String extraOptions, Listener listener, boolean installToUser) {
        installPackage(repoPackage, version, (PackageInstallScope) null, extraOptions, listener);
    }

    @Override
    public void updatePackage(InstalledPackage installedPackage, @Nullable String version, Listener listener) {
        String name = installedPackage.getName();
        String spec = name + "@" + (version == null ? "latest" : version);
        // the package stays in the section it is in
        PackageInstallScope scope = installedPackage instanceof NpmInstalledPackage npmPackage ? npmPackage.myScope : null;
        NpmPackageTool tool = NpmPackageTool.detect(getPackageDirectory());
        run(name, tool, tool.add(spec, scope, null), listener);
    }

    @Override
    public void uninstallPackages(List<InstalledPackage> installedPackages, Listener listener) {
        List<String> names = new ArrayList<>();
        for (InstalledPackage installedPackage : installedPackages) {
            names.add(installedPackage.getName());
        }
        NpmPackageTool tool = NpmPackageTool.detect(getPackageDirectory());
        run(names.size() == 1 ? names.get(0) : null, tool, tool.remove(names), listener);
    }

    private void run(@Nullable String packageName, NpmPackageTool tool, List<String> arguments, Listener listener) {
        String presentation = tool.getExecutable() + " " + String.join(" ", arguments);
        new Task.Backgroundable(myModule.getProject(), LocalizeValue.of(presentation), true) {
            @Override
            public void run(ProgressIndicator indicator) {
                listener.operationStarted(packageName);
                ErrorDescription error = null;
                try {
                    NpmExecutionSettings settings = NpmUtil.createExecutionSettings(myModule.getProject(), getPackageDirectory().toString());
                    NpmExecutionSettings toolSettings = tool == NpmPackageTool.NPM
                        ? settings
                        : new NpmExecutionSettings(tool.getExecutable(), settings.getNodeDirectory());
                    GeneralCommandLine commandLine = NpmUtil.createCommandLine(toolSettings, getPackageDirectory(), arguments);
                    ProcessOutput output = CapturingProcessUtil.execAndGetOutput(commandLine);
                    if (output.getExitCode() != 0) {
                        String message = StringUtil.notNullize(StringUtil.nullize(output.getStderr().trim()), "Exit code " + output.getExitCode());
                        error = new ErrorDescription(lastLine(message), commandLine.getCommandLineString(), output.getStdout() + output.getStderr(), null);
                    }
                }
                catch (ExecutionException e) {
                    error = ErrorDescription.fromMessage(StringUtil.notNullize(e.getMessage(), e.getClass().getName()));
                }
                // package.json is changed by the tool: the model of the module follows it
                VirtualFile packageJson = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(getPackageDirectory().resolve("package.json"));
                if (packageJson != null) {
                    packageJson.refresh(false, false);
                }
                NpmProjectModel.getInstance(myModule.getProject()).scheduleSync(myModule);
                listener.operationFinished(packageName, error);
            }
        }.queue();
    }

    private static String lastLine(String text) {
        List<String> lines = StringUtil.split(text, "\n");
        return lines.isEmpty() ? text : lines.get(lines.size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AsyncResult<List<String>> fetchPackageVersions(String packageName) {
        return async(() -> {
            Map<String, Object> metadata = metadata(packageName, true);
            List<String> versions = new ArrayList<>();
            if (metadata != null && metadata.get("versions") instanceof Map<?, ?> map) {
                for (Object version : map.keySet()) {
                    versions.add(String.valueOf(version));
                }
            }
            versions.sort((a, b) -> compareVersions(b, a));
            return versions;
        });
    }

    @Override
    public AsyncResult<String> fetchPackageDetails(String packageName) {
        String description = myDescriptions.get(packageName);
        if (description != null) {
            return AsyncResult.resolved(StringUtil.escapeXmlEntities(description));
        }
        return async(() -> {
            Map<String, Object> metadata = metadata(packageName, false);
            if (metadata == null) {
                return "";
            }
            StringBuilder html = new StringBuilder();
            if (metadata.get("description") instanceof String text) {
                myDescriptions.put(packageName, text);
                html.append(StringUtil.escapeXmlEntities(text));
            }
            if (metadata.get("homepage") instanceof String homepage) {
                html.append("<br/><a href=\"").append(StringUtil.escapeXmlEntities(homepage)).append("\">")
                    .append(StringUtil.escapeXmlEntities(homepage)).append("</a>");
            }
            return html.toString();
        });
    }

    @Override
    public boolean shouldFetchLatestVersionsForOnlyInstalledPackages() {
        return true;
    }

    @Override
    public AsyncResult<String> fetchLatestVersion(InstalledPackage pkg) {
        return async(() -> {
            Map<String, Object> metadata = metadata(pkg.getName(), true);
            if (metadata != null && metadata.get("dist-tags") instanceof Map<?, ?> tags && tags.get("latest") instanceof String latest) {
                return latest;
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    private static @Nullable Map<String, Object> metadata(String packageName, boolean abbreviated) throws IOException {
        // a scoped name keeps its @ and has its / encoded
        String url = REGISTRY + packageName.replace("/", "%2F");
        String text = HttpRequests.request(url).accept(abbreviated ? ABBREVIATED_METADATA : "application/json").productNameAsUserAgent().readString(null);
        return NpmPackageParser.readJson(text) instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    private static <T> AsyncResult<T> async(Callable<T> callable) {
        AsyncResult<T> result = AsyncResult.undefined();
        Application.get().executeOnPooledThread(() -> {
            try {
                result.setDone(callable.call());
            }
            catch (Exception e) {
                result.rejectWithThrowable(e);
            }
        });
        return result;
    }
}
