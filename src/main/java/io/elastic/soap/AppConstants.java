package io.elastic.soap;

/**
 * Class containing constants being used throughout the component
 */
public final class AppConstants {

    // Basic Authentication platform name
    public static final String BASIC_AUTH_CONFIG_NAME = "BASIC";
    // Name of binding object in the platform config
    public static final String BINDING_CONFIG_NAME = "binding";
    // Default package for generating JAXB classes to
    public static final String DEFAULT_PACKAGE = "io.elastic.soap";
    // Default folder for generating JAXB classes to
    public static final String GENERATED_RESOURCES_DIR = "src/main/generated-java";
    // Name of operation object in the platform config
    public static final String OPERATION_CONFIG_NAME = "operation";
    // SOAP 1.1 short name
    public static final String SOAP11_PROTOCOL_NAME = "SOAP11";
    // SOAP 1.2 short name
    public static final String SOAP12_PROTOCOL_NAME = "SOAP12";
    // Name of WSDL URI object in the platform config
    public static final String WSDL_CONFIG_NAME = "wsdlURI";
    // Name of WSDL URI object in the platform config
    public static final String WSDL_LOCAL_PATH = "src/main/generated-java/wsdlLocal.xml";
    // Authorization keyword
    public static final String AUTH_KEYWORD = "Authorization";
    // component.json validation option keyword
    public static final String VALIDATION = "validation";
    // Validation enabled keyword
    public static final String VALIDATION_ENABLED = "enabled";
    // Validation disabled keyword
    public static final String VALIDATION_DISABLED = "disabled";

    // Private constructor to prevent instantiation. Since utility classes should not be instantiated
    private AppConstants() {
    }

}
