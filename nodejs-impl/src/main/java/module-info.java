/**
 * @author VISTALL
 * @since 08/01/2023
 */
module consulo.nodejs {
    requires java.net.http;

    requires consulo.nodejs.api;

    requires consulo.json.api;
    requires consulo.json.jom.api;
    
    requires consulo.javascript.cdt.debugger.impl;

    // TODO remove in future
    requires java.desktop;
    requires cdt.java.client;

    opens consulo.nodejs.impl.run to consulo.util.xml.serializer;
}