package io.elastic.soap.compilers.generators.impl;

import io.elastic.soap.AppConstants;
import io.elastic.soap.compilers.generators.IJaxbGenerator;
import io.elastic.soap.utils.Utils;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.apache.axis2.wsdl.WSDL2Java;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Axis2GeneratorImpl implements IJaxbGenerator {

  private static final Logger logger = LoggerFactory.getLogger(Axis2GeneratorImpl.class);

  /**
   * Parses WSDL and generates compiled JAXB structure using Apache Axis2 tool. {@link
   * WsImportGeneratorImpl} may also be used instead of this implementation, but it has some
   * limitations. In JAX-WS RPC/encoded is not supported as a messaging mode. In JAX-WS the
   * “encoded” encoding style isn’t supported and only the “literal” encoding style used. In general
   * case using {@link Axis2GeneratorImpl} is preferred.
   *
   * @param wsdlUrl WSDL URL
   * @param wsdlCompiledClassesCacheMap Map that consists of WSDL URL as a key and corresponding
   * path as a value. It is used for caching. If there is a key-value pair in the map already, then
   * it means that the JAXB structure is already generated and there is no need to generate the JAXB
   * structure once more.
   * @return Path of the JAXB structure where it has been generated
   */
  @Override
  public String generateJaxbClasses(String wsdlUrl, Map<String, String> wsdlCompiledClassesCacheMap)
      throws Exception {
    logger.info(
        "About to start generating JAXB structure. javax.xml.accessExternalSchema will be enabled...");
    System.setProperty("javax.xml.accessExternalSchema", "all");
    String path = wsdlCompiledClassesCacheMap.get(wsdlUrl);
    if (path != null) {
      return path;
    }
    // -Djavax.xml.accessExternalSchema all - restrict access to the protocols specified for external reference set by the
    // schemaLocation attribute, Import and Include element.
    // -uri WSDL file location. This should point to a WSDL file in the local file system.
    // -d specifies the directory into which the generated code files are written.
    // -or overwrites the existing classes.
    // -p. The target package name. If omitted, a default package (formed using the target
    // namespace of the WSDL) will be used.
    // --noBuildXML - don't generate the build.xml in the output directory
    String[] input = new String[]{
        "-Djavax.xml.accessExternalSchema", "all",
        "-uri", wsdlUrl,
        "-d", "jaxbri",
        "-or",
        "-p", AppConstants.DEFAULT_PACKAGE + ".ignored",
        "--noBuildXML",
        "-ep", AppConstants.DEFAULT_PACKAGE + ".ignored"
    };

    WSDL2Java.main(input);
    logger.info("JAXB structure was successfully generated");
    logger.info("About to start compiling generated JAXB classes...");
    List<Path> paths = Utils.listGeneratedFiles("src");
    // Now we must compile JAXB classes as Apache WSDL2Java tool does not compile generated classes
    for (Path p : paths) {
      com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
      // -cp path option specifies where to find user class files and annotation processors.
      // This class path overrides the user class path in the CLASSPATH environment variable.
      // -proc controls whether annotation processing and compilation are done.
      // -proc:none means that compilation takes place without annotation processing.
      String[] options = new String[]{"-d", AppConstants.GENERATED_RESOURCES_DIR,
          "-cp", "src",
          "-proc:none",
          p.toString()};
      javac.compile(options);
    }
    path = AppConstants.GENERATED_RESOURCES_DIR;
    wsdlCompiledClassesCacheMap.put(wsdlUrl, AppConstants.GENERATED_RESOURCES_DIR);
    logger.info("Generated JAXB structure was successfully compiled");
    return path;
  }
}
