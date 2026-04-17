/**
 * @author VISTALL
 * @since 08/01/2023
 */
module consulo.nodejs {
    requires consulo.nodejs.api;

    requires consulo.json.api;
    requires consulo.json.jom.api;
    
    requires consulo.javascript.cdt.debugger.impl;

    requires consulo.ide.api;
    requires consulo.application.api;
    requires consulo.application.content.api;
    requires consulo.configurable.api;
    requires consulo.disposer.api;
    requires consulo.document.api;
    requires consulo.execution.api;
    requires consulo.execution.impl;
    requires consulo.execution.debug.api;
    requires consulo.file.editor.api;
    requires consulo.http.api;
    requires consulo.language.api;
    requires consulo.localize.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires consulo.module.ui.api;
    requires consulo.platform.api;
    requires consulo.process.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.ui.ex.awt.api;
    requires consulo.virtual.file.system.api;
    requires consulo.util.collection;
    requires consulo.util.dataholder;
    requires consulo.util.io;
    requires consulo.util.lang;

    // TODO remove in future
    requires java.desktop;
    requires cdt.java.client;

    opens consulo.nodejs.impl.run to consulo.util.xml.serializer;
}