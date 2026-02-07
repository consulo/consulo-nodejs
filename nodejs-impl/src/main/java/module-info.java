/**
 * @author VISTALL
 * @since 08/01/2023
 */
module consulo.nodejs {
    requires consulo.nodejs.api;

    requires consulo.json.api;
    requires consulo.json.jom.api;
    
    requires consulo.javascript.v8.debugger.impl;

    // TODO remove in future
    requires java.desktop;

    opens consulo.nodejs.impl.run to consulo.util.xml.serializer;
}