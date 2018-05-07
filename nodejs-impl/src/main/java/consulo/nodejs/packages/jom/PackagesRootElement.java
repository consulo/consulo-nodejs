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

package consulo.nodejs.packages.jom;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import consulo.json.jom.JomElement;
import consulo.json.jom.JomPropertyGetter;

/**
 * @author VISTALL
 * @since 04.12.2015
 */
public interface PackagesRootElement extends JomElement
{
	@JomPropertyGetter
	String getName();

	@JomPropertyGetter
	String getVersion();

	@JomPropertyGetter
	boolean isPreferGlobal();

	@JomPropertyGetter
	String getDescription();

	@JomPropertyGetter
	Set<String> getContributors();

	@JomPropertyGetter
	Set<String> getFiles();

	@Nonnull
	@JomPropertyGetter
	Map<String, Boolean> getConfig();

	@Nonnull
	@JomPropertyGetter
	Map<String, String> getEngines();

	@Nonnull
	@JomPropertyGetter
	Map<String, String> getDirectories();

	@Nonnull
	@JomPropertyGetter
	Map<String, String> getScripts();

	@JomPropertyGetter
	String getRepository();

	@Nonnull
	@JomPropertyGetter
	Set<String> getKeywords();

	@JomPropertyGetter
	String getAuthor();

	@JomPropertyGetter
	String getLicense();

	@Nonnull
	@JomPropertyGetter
	Map<String, String> getBugs();

	@JomPropertyGetter
	String getHomepage();

	@JomPropertyGetter
	String getMain();

	@JomPropertyGetter
	String getBin();

	@Nonnull
	@JomPropertyGetter
	Map<String, String> getDependencies();

	@Nonnull
	@JomPropertyGetter
	Set<String> getBundleDependencies();

	@Nonnull
	@JomPropertyGetter
	Map<String, String> getDevDependencies();
}
