/**
 * @author VISTALL
 * @since 08/01/2023
 */
module consulo.mocha
{
	requires consulo.nodejs.api;

	requires consulo.ide.api;
	requires consulo.module.ui.api;
	requires consulo.application.api;
	requires consulo.application.content.api;
	requires consulo.configurable.api;
	requires consulo.container.api;
	requires consulo.disposer.api;
	requires consulo.execution.api;
	requires consulo.execution.impl;
	requires consulo.execution.test.api;
	requires consulo.execution.test.sm.api;
	requires consulo.file.chooser.api;
	requires consulo.language.api;
	requires consulo.localize.api;
	requires consulo.module.api;
	requires consulo.module.content.api;
	requires consulo.process.api;
	requires consulo.project.api;
	requires consulo.ui.api;
	requires consulo.ui.ex.awt.api;
	requires consulo.virtual.file.system.api;
	requires consulo.util.io;
	requires consulo.util.lang;

	// TODO remove in future
	requires java.desktop;

	exports consulo.mocha.icon;
	exports consulo.mocha.module;
	exports consulo.mocha.module.extension;
	exports consulo.mocha.psi;
	exports consulo.mocha.run;
	exports consulo.mocha.localize;
}