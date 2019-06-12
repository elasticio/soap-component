package io.elastic.soap.compilers.generators.impl;

import com.sun.tools.ws.WsImport;
import io.elastic.soap.AppConstants;
import io.elastic.soap.compilers.generators.IJaxbGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WsImportGeneratorImpl implements IJaxbGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsImportGeneratorImpl.class);

    /**
     * Parses WSDL and generates compiled JAXB structure using {@link WsImport} tool. It has some
     * limitations. In JAX-WS RPC/encoded is not supported as a messaging mode. In JAX-WS the
     * "encoded" encoding style isn’t supported and only the "literal" encoding style used. In this
     * case using {@link Axis2GeneratorImpl} is preferred.
     *
     * @param wsdlUrl                     WSDL URL
     * @param wsdlCompiledClassesCacheMap Map that consists of WSDL URL as a key and corresponding
     *                                    path as a value. It is used for caching. If there is a key-value pair in the map already, then
     *                                    it means that the JAXB structure is already generated and there is no need to generate the JAXB
     *                                    structure once more.
     * @return Path of the JAXB structure where it has been generated
     */
    @Override
    public String generateJaxbClasses(final String wsdlUrl,
                                      final Map<String, String> wsdlCompiledClassesCacheMap) throws Throwable {
        LOGGER.info("About to start generating JAXB structure...");
        String path = wsdlCompiledClassesCacheMap.get(wsdlUrl);
        if (path == null) {
            final String[] input = new String[]{
                    "-d", AppConstants.GENERATED_RESOURCES_DIR,
                    "-p", AppConstants.DEFAULT_PACKAGE,
                    wsdlUrl};

            WsImport.doMain(input);
            path = AppConstants.GENERATED_RESOURCES_DIR;
            wsdlCompiledClassesCacheMap.put(wsdlUrl, AppConstants.GENERATED_RESOURCES_DIR);
            LOGGER.info("JAXB structure was successfully created");
        }
        return path;
    }
}
