/**
 * @author VISTALL
 * @since 08/01/2023
 */
module consulo.mocha
{
	requires consulo.nodejs.api;

	// TODO remove in future
	requires java.desktop;

	exports consulo.mocha.icon;
	exports consulo.mocha.module;
	exports consulo.mocha.module.extension;
	exports consulo.mocha.psi;
	exports consulo.mocha.run;
}