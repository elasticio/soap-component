package io.elastic.soap.services;

import com.predic8.wsdl.Definitions;

import javax.json.JsonObject;
import java.io.IOException;

public interface WSDLService {

    /**
     * Get WSDL from provided source.
     * @param config component config.
     * @return wsdl.
     * @throws IOException if it happens.
     */
    Definitions getWSDL(final JsonObject config) throws IOException;
}
