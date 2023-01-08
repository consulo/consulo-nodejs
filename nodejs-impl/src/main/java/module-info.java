/**
 * @author VISTALL
 * @since 08/01/2023
 */
module consulo.nodejs
{
	requires consulo.nodejs.api;

	requires consulo.javascript.json.javascript.impl;
	requires consulo.javascript.v8.debugger.impl;

	// TODO remove in future
	requires java.desktop;
}