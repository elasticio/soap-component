package io.elastic.soap.compilers.generators.impl;

import io.elastic.soap.AppConstants;
import io.elastic.soap.compilers.generators.IJaxbGenerator;
import io.elastic.soap.utils.Utils;
import org.apache.axis2.wsdl.WSDL2Java;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Axis2GeneratorImpl implements IJaxbGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Axis2GeneratorImpl.class);

    /**
     * Parses WSDL and generates compiled JAXB structure using Apache Axis2 tool. {@link
     * WsImportGeneratorImpl} may also be used instead of this implementation, but it has some
     * limitations. In JAX-WS RPC/encoded is not supported as a messaging mode. In JAX-WS the
     * "encoded" encoding style isn’t supported and only the "literal" encoding style used. In general
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
                                      final Map<String, String> wsdlCompiledClassesCacheMap) throws Exception {
        LOGGER.info("About to start generating JAXB structure. javax.xml.accessExternalSchema will be enabled...");
        System.setProperty("javax.xml.accessExternalSchema", "all");
        String path = wsdlCompiledClassesCacheMap.get(wsdlUrl);
        if (path == null) {
            // -Djavax.xml.accessExternalSchema all - restrict access to the protocols specified for external reference set by the
            // schemaLocation attribute, Import and Include element.
            // -uri WSDL file location. This should point to a WSDL file in the local file system.
            // -d specifies the directory into which the generated code files are written.
            // -or overwrites the existing classes.
            // -p. The target package name. If omitted, a default package (formed using the target
            // namespace of the WSDL) will be used.
            // --noBuildXML - don't generate the build.xml in the output directory
            final String[] input = new String[]{
                    "-Djavax.xml.accessExternalSchema", "all",
                    "-uri", wsdlUrl,
                    "-d", "jaxbri",
                    "-or",
                    "-p", AppConstants.DEFAULT_PACKAGE + ".ignored",
                    "--noBuildXML",
                    "-ep", AppConstants.DEFAULT_PACKAGE + ".ignored"
            };
            PrintStream originalStdout = System.out;
            System.setOut(new PrintStream(new OutputStream() { // Hack coz logs of lib contains sensitive info. Disabling System.out.println.
                @Override
                public void write(int i) {

                }
            }));
            WSDL2Java.main(input);
            System.setOut(originalStdout);
            LOGGER.info("JAXB structure was successfully generated");
            LOGGER.info("About to start compiling generated JAXB classes...");
            final List<Path> paths = Utils.listGeneratedFiles("src");
            // Now we must compile JAXB classes as Apache WSDL2Java tool does not compile generated classes
            for (final Path p : paths) {
                final com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
                // -cp path option specifies where to find user class files and annotation processors.
                // This class path overrides the user class path in the CLASSPATH environment variable.
                // -proc controls whether annotation processing and compilation are done.
                // -proc:none means that compilation takes place without annotation processing.
                final String[] options = new String[]{"-d", AppConstants.GENERATED_RESOURCES_DIR,
                        "-cp", "src",
                        "-proc:none",
                        p.toString()};
                javac.compile(options);
            }
            path = AppConstants.GENERATED_RESOURCES_DIR;
            wsdlCompiledClassesCacheMap.put(wsdlUrl, AppConstants.GENERATED_RESOURCES_DIR);
            LOGGER.info("JAXB structure was successfully created");
        }
        return path;
    }
}
