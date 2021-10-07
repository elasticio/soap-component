package io.elastic.soap.compilers;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.predic8.schema.Schema;
import com.predic8.wsdl.Binding;
import com.predic8.wsdl.BindingOperation;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.WSDLParser;
import io.elastic.soap.AppConstants;
import io.elastic.soap.compilers.generators.IJaxbGenerator;
import io.elastic.soap.compilers.generators.JaxbGeneratorModule;
import io.elastic.soap.compilers.generators.impl.Axis2GeneratorImpl;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.elastic.soap.utils.Utils.getElementName;

/**
 * Class for generating JAXB classes structure and loading it into classloader in order to make it
 * accessible in runtime.
 * <p>
 * One of the available implementations can be used: {@link io.elastic.soap.compilers.generators.impl.WsImportGeneratorImpl}
 * or {@link Axis2GeneratorImpl}
 * <p>
 * {@link io.elastic.soap.compilers.generators.impl.WsImportGeneratorImpl} has some limitations. In
 * JAX-WS RPC/encoded is not supported as a messaging mode. In JAX-WS the "encoded" encoding style
 * isn’t supported and only the "literal" encoding style used. In most cases using {@link
 * Axis2GeneratorImpl} is preferred.
 */
public class JaxbCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaxbCompiler.class);
    /**
     * One of two available IJaxbGenerator implementations is injected here. It can be done in {@link
     * JaxbGeneratorModule} class
     */
    @Inject
    private static IJaxbGenerator generator;
    private static Map<String, String> isWsdlCompiledMap = new HashMap<>();
    private static Map<String, Definitions> loadedDefsMap = new HashMap<>();

    /**
     * Check directory for generating JAXB classes in. If it does not exist, create it.
     */
    static {
        createFolder(AppConstants.GENERATED_RESOURCES_DIR);
    }

    /**
     * Creates {@link Injector} in order to inject needed implementation of {@link IJaxbGenerator}
     */
    static {
        generator = injectJaxbGeneratorModule(IJaxbGenerator.class);
    }

    /**
     * Check directory for generating JAXB classes in. If it does not exist, create it.
     */
    public static void createFolder(final String path) {
        final File dir = new File(path);
        if (!dir.exists()) {
            boolean mkDirResult = dir.mkdirs();
            if (mkDirResult) {
                LOGGER.trace("Directory {} successfully created.", AppConstants.GENERATED_RESOURCES_DIR);
            } else {
                throw new RuntimeException(
                        "Folder for storing generated classes could not be created. "
                                + "The component execution will be terminated");
            }
        }
    }

    /**
     * Generates JAXB structure and loads it into classpath
     */
    public static void generateAndLoadJaxbStructure(final String wsdlUrl) throws Throwable {
        generator.generateJaxbClasses(wsdlUrl, isWsdlCompiledMap);
        loadClassesInDirToClassloader();
    }

    /**
     * Method filters out all the bindings and operations except for the input one pair.
     *
     * @param defs      {@link Definitions} object of the parsed WSDL
     * @param binding   Binding name
     * @param operation Operation name
     * @return {@link BindingOperation} object
     */
    public static BindingOperation getBindingOperation(final Definitions defs, final String binding,
                                                       final String operation) {
        final List<Binding> bindingList = defs.getBindings();
        return bindingList.stream()
                .filter(bind -> bind.getName().equals(binding))
                .findAny().get()
                .getOperations().stream()
                .filter(bindingOp -> bindingOp.getName().equals(operation))
                .findAny().get();
    }

    /**
     * Method parses the WSDL for the given bending/operation pair, creates and returns a {@link
     * SoapBodyDescriptor} object containing this data combination
     */
    public static SoapBodyDescriptor getSoapBodyDescriptor(final String wsdlUrl, final String binding,
                                                           final String operation) {
        Definitions defs = loadedDefsMap.get(wsdlUrl);
        if (defs == null) {
            defs = new JaxbCompiler().getDefinitionsFromWsdl(wsdlUrl);
            loadedDefsMap.put(wsdlUrl, defs);
        }
        final BindingOperation bindingOperation = getBindingOperation(defs, binding, operation);
        LOGGER.trace("Got {} style wsdl", bindingOperation.getBinding().getStyle());
        if (bindingOperation.getBinding().getStyle().equals("Rpc/Encoded")) {
            LOGGER.error("SOAP component currently doesn't support the rpc/encoded style {}",
                    bindingOperation.getBinding().getStyle());
            throw new UnsupportedOperationException(
                    "SOAP component currently doesn't support the rpc/encoded style");
        }
        String soapAction;
        if(bindingOperation.getOperation() != null) {
            soapAction = bindingOperation.getOperation().getSoapAction();
        } else {
            soapAction = bindingOperation.getName();
        }
        final String soapEndPoint = defs.getServices().stream()
                .flatMap(service -> service.getPorts().stream())
                .filter(port -> port.getBinding().getName().equals(binding))
                .findAny()
                .orElseThrow(() -> new ComponentException("Port not found for binding"))
                .getAddress()
                .getLocation();
        final Message inputMessage = (Message) bindingOperation.getInput().getProperty("message");
        final Message outputMessage = (Message) bindingOperation.getOutput().getProperty("message");
        final String inputElementName = getElementName(inputMessage);
        final String outputElementName = getElementName(outputMessage);
        return new SoapBodyDescriptor.Builder()
                .setBindingName(binding).setOperationName(operation).setSoapAction(soapAction)
                .setSoapEndPoint(soapEndPoint).setRequestBodyElementName(inputElementName)
                .setRequestBodyPackageName(Utils.convertToPackageName(inputMessage.getNamespaceUri()))
                .setRequestBodyNameSpace(inputMessage.getParts().get(0).getElement().getNamespaceUri())
                .setRequestBodyClassName(getClassName(inputMessage, inputElementName))
                .setResponseBodyElementName(outputElementName)
                .setRequestBodyPackageName(Utils.convertToPackageName(inputMessage.getNamespaceUri()))
                .setResponseBodyNameSpace(outputMessage.getNamespaceUri())
                .setResponseBodyClassName(getClassName(outputMessage, outputElementName)).build();
    }

    /**
     * Creates {@link Injector} in order to inject needed implementation of {@link IJaxbGenerator}
     */
    public static IJaxbGenerator injectJaxbGeneratorModule(final Class clazz) {
        final Injector injector = Guice.createInjector(new JaxbGeneratorModule());
        return (IJaxbGenerator) injector.getInstance(clazz);
    }

    /**
     * Classes, generated by {@link IJaxbGenerator#generateJaxbClasses(String, Map)} should be loaded
     * into classloader in order to be accessible
     */
    public static void loadClassesInDirToClassloader()
            throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LOGGER.info("About to start loading generated JAXB classes into class loader");
        final File file = new File(AppConstants.GENERATED_RESOURCES_DIR);
        final URL url = file.toURI().toURL();
        final URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        final Class urlClass = URLClassLoader.class;
        final Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(urlClassLoader, new Object[]{url});
        LOGGER.info("Loading generated JAXB classes into class loader successfully done");
    }

    /**
     * Some XSD schemas have elements with its types, some - have no types. In order to parse all the
     * elements properly, this method takes either element name or type name.
     *
     * @param msg         WSDL {@link Message} representation
     * @param elementName The name of the element which type or element name should be retieved
     * @return Element or type name
     */
    public static String getClassName(final Message msg, final String elementName) {
        String className;
        if (msg.getParts().get(0).getElement().getType() == null) {
            className = Utils.convertStringToUpperCamelCase(elementName);
        } else {
            className = msg.getParts().get(0).getElement().getType().getLocalPart();
            className = Utils.convertStringToUpperCamelCase(className);
        }
        return Utils.convertToPackageName(msg.getNamespaceUri()) + "." + className;
    }

    /**
     * Method calls external WSDL by its URL and parses it
     *
     * @return {@link Definitions} object
     */
    public static Definitions getDefinitionsFromWsdl(final String wsdlUrl) {
        final WSDLParser parser = new WSDLParser();
        return parser.parse(wsdlUrl);
    }

    public static void putToCache(final String url, final String path) {
        isWsdlCompiledMap.put(url, path);
    }

    /**
     * Utility method which you can run to build a schema for given WSDL
     * @param args
     */
    public static void main(String[] args) {
        Definitions definitions = getDefinitionsFromWsdl(
                "http://ws.cdyne.com/emailverify/Emailvernotestemail.asmx?wsdl");
        List<Schema> schemas = definitions.getSchemas();
        for (Schema schema : schemas) {
            System.out.println(schema.toString());
        }
    }
}
