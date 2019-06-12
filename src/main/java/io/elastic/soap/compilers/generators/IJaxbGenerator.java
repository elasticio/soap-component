package io.elastic.soap.compilers.generators;

import com.sun.tools.ws.WsImport;
import io.elastic.soap.compilers.generators.impl.Axis2GeneratorImpl;
import java.util.Map;

public interface IJaxbGenerator {

  /**
   * Parses WSDL and generates compiled JAXB structure using {@link WsImport} tool. It has some
   * limitations. In JAX-WS RPC/encoded is not supported as a messaging mode. In JAX-WS the
   * "encoded" encoding style isn’t supported and only the "literal" encoding style used. In this
   * case using {@link Axis2GeneratorImpl} is preferred.
   *
   * @param wsdlUrl WSDL URL
   * @param wsdlCompiledClassesCacheMap Map that consists of WSDL URL as a key and corresponding
   * path as a value. It is used for caching. If there is a key-value pair in the map already, then
   * it means that the JAXB structure is already generated and there is no need to generate the JAXB
   * structure once more.
   * @return Path of the JAXB structure where it has been generated
   */
  String generateJaxbClasses(String wsdlUrl, Map<String, String> wsdlCompiledClassesCacheMap)
      throws Throwable;
}
