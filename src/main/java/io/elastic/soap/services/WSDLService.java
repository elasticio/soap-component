package io.elastic.soap.services;

import com.predic8.wsdl.Definitions;

import javax.json.JsonObject;
import java.io.IOException;

public interface WSDLService {

    /**
     * Get WSDL from provided source.
     *
     * @param config component config.
     * @return wsdl.
     * @throws IOException if it happens.
     */
    Definitions getWSDL(final JsonObject config) throws IOException;

    /**
   * Fetches the WSDL from the provided URL or local path.
   *
   * @param wsdlUrl WSDL URL or local path.
   * @return {@link Definitions} object representing the WSDL.
   * @throws IOException if WSDL can't be fetched.
   */
  Definitions getWSDL(final String wsdlUrl) throws IOException;
}
