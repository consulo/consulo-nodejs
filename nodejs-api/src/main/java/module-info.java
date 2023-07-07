/**
 * @author VISTALL
 * @since 08/01/2023
 */
module consulo.nodejs.api
{
	requires transitive consulo.ide.api;

	requires transitive consulo.javascript.base.api;

	// TODO remove in future
	requires java.desktop;

	exports consulo.nodejs;
	exports consulo.nodejs.bundle;
	exports consulo.nodejs.icon;
	exports consulo.nodejs.module.extension;
	exports consulo.nodejs.packages.call;
	exports consulo.nodejs.run;
	exports consulo.nodejs.localize;
}