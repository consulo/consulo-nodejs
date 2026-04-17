/**
 * @author VISTALL
 * @since 08/01/2023
 */
module consulo.nodejs.api
{
    requires transitive consulo.ide.api;
    requires transitive consulo.javascript.base.api;

    requires consulo.application.api;
    requires consulo.application.content.api;
    requires consulo.component.api;
    requires consulo.configurable.api;
    requires consulo.container.api;
    requires consulo.execution.api;
    requires consulo.execution.impl;
    requires consulo.file.chooser.api;
    requires consulo.language.api;
    requires consulo.localize.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires consulo.module.ui.api;
    requires consulo.platform.api;
    requires consulo.process.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.awt.api;
    requires consulo.virtual.file.system.api;
    requires consulo.util.collection;
    requires consulo.util.io;
    requires consulo.util.lang;
    requires consulo.util.xml.serializer;

    // TODO remove in future
    requires java.desktop;

    exports consulo.nodejs;
    exports consulo.nodejs.bundle;
    exports consulo.nodejs.icon;
    exports consulo.nodejs.module.extension;
    exports consulo.nodejs.packages.call;
    exports consulo.nodejs.run;
    exports consulo.nodejs.localize;

    opens consulo.nodejs.run to consulo.util.xml.serializer;
}