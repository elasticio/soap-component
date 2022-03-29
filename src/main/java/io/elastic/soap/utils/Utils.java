package io.elastic.soap.utils;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.base.CaseFormat;
import com.sun.xml.bind.api.impl.NameConverter;
import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.soap.AppConstants;
import io.elastic.soap.compilers.JaxbCompiler;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.services.SoapCallService;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public final class Utils {

  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
  private static final int REQUEST_TIMEOUT = 60000;

  // Private constructor to prevent instantiation. Since utility classes should not be instantiated
  private Utils() {
  }

  /**
   * Create configured ObjectMapper. All the marshall/unmarshal configurations should be added here
   * if needed
   *
   * @return ObjectMapper instance
   */
  public static ObjectMapper getConfiguredObjectMapper() {
    final JaxbAnnotationModule module = new JaxbAnnotationModule();
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    objectMapper.registerModule(new JSR353Module());
    objectMapper.registerModule(module);
    return objectMapper;
  }

  /**
   * Method to get Binding from the message config.
   *
   * @param config {@link io.elastic.api.Message} configuration object.
   * @return String representation of the Binding.
   */
  public static String getBinding(final JsonObject config) {
    final String binding = Utils.getConfigParam(config, AppConstants.BINDING_CONFIG_NAME);
    return binding;
  }

  /**
   * Method to get Operation from the message config.
   *
   * @param config {@link io.elastic.api.Message} configuration object.
   * @return String representation of the Operation.
   */
  public static String getOperation(final JsonObject config) {
    final String operation = Utils.getConfigParam(config, AppConstants.OPERATION_CONFIG_NAME);
    return operation;
  }

  /**
   * Since many WSDL schemas have XSD elements started from the small letter (getBank), with
   * underscore (CustomerQueryIn_sync), but WSImport utility generates this class starting from a
   * capital letter (GetBank) and without underscores (CustomerQueryInSync), it should be manually
   * converted to upper camel case.
   */
  public static String convertStringToUpperCamelCase(final String elementName) {
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
        CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, elementName));
  }

  /**
   * Method to get WSDL URL from the message config.
   *
   * @param config {@link io.elastic.api.Message} configuration object.
   * @return String representation of the WSDL URL.
   */
  public static String getWsdlUrl(final JsonObject config) {
    return Utils.getConfigParam(config, AppConstants.WSDL_CONFIG_NAME);
  }

  /**
   * Add auth to URL in form of https://username:password@example.com
   *
   * @param source source of resource.
   * @param username username.
   * @param password password.
   * @return URI with username and password.
   */
  public static String addAuthToURL(final String source, final String username,
      final String password) {
    try {
      final URI uri = new URI(source);
      return new URI(uri.getScheme(), username + ":" + password, uri.getHost(), uri.getPort(),
          uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
    } catch (URISyntaxException e) {
      throw new ComponentException("Invalid URI: " + source, e);
    }

  }

  /**
   * Check if basic auth credentials are enabled
   *
   * @return true if basic auth is enabled. false otherwise
   */
  public static boolean isBasicAuth(JsonObject config) {
    final String authType = config.getJsonObject("auth").getJsonString("type").getString();
    if (!AppConstants.BASIC_AUTH_CONFIG_NAME.equals(authType)) {
      return false;
    }
    final String username = Optional.ofNullable(getUsername(config)).orElse("");
    final String password = Optional.ofNullable(getPassword(config)).orElse("");
    return !username.equals("") && !password.equals("");
  }

  /**
   * Retrieves username from the credentials object
   */
  public static String getUsername(JsonObject config) {
    return config.getJsonObject("auth").getJsonObject("basic").getString("username");
  }

  /**
   * Retrieves password from the credentials object
   */
  public static String getPassword(JsonObject config) {
    return config.getJsonObject("auth").getJsonObject("basic").getString("password");
  }

  public static boolean getEmitSoapFault(JsonObject config) {
    return Utils.getConfigBoolean(config, "emitSoapFault");
  }

  /**
   * Internal common method for getting value from the configuration {@link JsonObject} object.
   *
   * @param config Platform message's config. {@link JsonObject} type.
   * @param key String value of key to find the value of.
   * @return String value
   */
  private static String getConfigParam(final JsonObject config, final String key) {
    final JsonString value = config.getJsonString(key);

    if (value == null) {
      throw new IllegalStateException(String.format("Config parameter '%s' is required", key));
    }

    return value.getString();
  }

  private static boolean getConfigBoolean(final JsonObject config, final String key) {
    return config.getBoolean(key, false);
  }

  /**
   * Retrieves password from the credentials object
   */
  public static int getRequestTimeoutPeriod(JsonObject config) {
    try {
      LOGGER.info("Try to parse requestTimeoutPeriod value...");
      int timeout = Integer.parseInt(config.getString("requestTimeoutPeriod"));
      LOGGER.info("RequestTimeoutPeriod value is {}", timeout);
      return timeout;
    } catch (Exception e) {
      LOGGER.info("Default timeout value = {} will be returned", REQUEST_TIMEOUT);
      return REQUEST_TIMEOUT;
    }
  }

  public static JsonObjectBuilder buildJsonSoapFault(SOAPFault soapFault) {
    String soapFaultNamespaceURI = soapFault.getNamespaceURI();
    SoapVersion soapVersion = Utils.resolveSoapVersion(soapFaultNamespaceURI);
    switch (soapVersion) {
      case SOAP1_1:
        return Utils.buildJsonSoapFaultSoap11(soapFault);
      case SOAP1_2:
        try {
          return Utils.buildJsonSoapFaultSoap12(soapFault);
        } catch (SOAPException e) {
          e.printStackTrace();
          throw new ComponentException(e.getMessage());
        }
      default:
        throw new ComponentException("SOAP Version was not resolved");
    }
  }

  private static SoapVersion resolveSoapVersion(String namespace) {
    if (namespace.contains("schemas.xmlsoap.org/soap/envelope")) {
      return SoapVersion.SOAP1_1;
    }
    if (namespace.contains("www.w3.org/2003/05/soap-envelope")) {
      return SoapVersion.SOAP1_2;
    }
    throw new ComponentException("SOAP Version was not resolved");
  }

  private static JsonObjectBuilder buildJsonSoapFaultSoap11(SOAPFault soapFault) {
    JsonValue faultCode = Utils.getValueOrNull("faultcode", soapFault.getFaultCode());
    JsonValue faultString = Utils.getValueOrNull("faultstring", soapFault.getFaultString());
    JsonValue faultActor = Utils.getValueOrNull("faultactor", soapFault.getFaultActor());
    JsonObjectBuilder jsonSoapFaultBuilder = Json.createObjectBuilder()
        .add("Fault", Json.createObjectBuilder()
            .add("faultcode", faultCode)
            .add("faultstring", faultString)
            .add("faultactor", faultActor)
            .build());
    return jsonSoapFaultBuilder;
  }

  private static JsonObjectBuilder buildJsonSoapFaultSoap12(SOAPFault soapFault)
      throws SOAPException {
    JsonValue faultCode = Utils.getValueOrNull("faultcode", soapFault.getFaultCode());
    JsonArrayBuilder reasonArrayBuilder = Json.createArrayBuilder();
    Iterator faultReasonTexts = soapFault.getFaultReasonTexts();
    while (faultReasonTexts.hasNext()) {
      reasonArrayBuilder.add((String) faultReasonTexts.next());
    }
    JsonObjectBuilder jsonSoapFaultBuilder = Json.createObjectBuilder()
        .add("Fault", Json.createObjectBuilder()
            .add("faultcode", faultCode)
            .add("reason", reasonArrayBuilder.build())
            .build());
    return jsonSoapFaultBuilder;
  }

  private static JsonValue getValueOrNull(String key, String value) {
    if (value == null) {
      return JsonValue.NULL;
    }
    return Json.createObjectBuilder().add(key, value).build().get(key);
  }

  /**
   * Internal common method for getting list of generated java classes {@link JsonObject} object.
   *
   * @param path path to root folder. {@link Path} type.
   * @return List of Path values
   */
  public static List<Path> listGeneratedFiles(final String path) throws IOException {
    final Path source = Paths.get(path);
    return Files.walk(source).filter(Files::isRegularFile)
        .filter(
            pathFilter -> !pathFilter.startsWith("src/main") && !pathFilter.startsWith("src/test"))
        .collect(Collectors.toList());
  }

  public static String convertToPackageName(final String xmlNamespace) {
    final NameConverter nameConverter = new NameConverter.Standard();
    return nameConverter.toPackageName(xmlNamespace);
  }

  public static String getElementName(final com.predic8.wsdl.Message message) {
    if (message.getParts().size() == 0 || message.getParts().get(0).getElement() == null){
      return null;
    }
    return message.getParts().get(0).getElement().getName();
  }

  /**
   * Create GET request for URI, username, password.
   *
   * @return get request.
   */
  public static HttpGet createGet(final JsonObject config) {
    final HttpGet get = new HttpGet(getWsdlUrl(config));
    if (Utils.isBasicAuth(config)) {
      final String username = Utils.getUsername(config);
      final String password = Utils.getPassword(config);
      LOGGER.info("Adding Authorization header");
      get.addHeader("Authorization", "Basic " + Base64Utils.getEncodedString(username, password));
    }
    return get;
  }

  /**
   * Loads WSDL file locally and parses it from the FS. Is a workaround for the basic auth case when
   * Server fault: too many redirects (20)
   */
  public static String loadWsdlLocally(final JsonObject configuration) throws IOException {
    final String username = Utils.getUsername(configuration);
    final String password = Utils.getPassword(configuration);
    final URL url = new URL(Utils.getWsdlUrl(configuration));
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    final String auth = username + ":" + password;
    byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
    String authHeaderValue = "Basic " + new String(encodedAuth);
    connection.setRequestProperty("Authorization", authHeaderValue);

    InputStream is = connection.getInputStream();
    FileOutputStream fos = new FileOutputStream(new File(AppConstants.WSDL_LOCAL_PATH));
    byte[] buffer = new byte[4096];
    int n = 0;
    while (-1 != (n = is.read(buffer))) {
      fos.write(buffer, 0, n);
    }
    is.close();
    fos.close();
    return AppConstants.WSDL_LOCAL_PATH;
  }

  public static SoapBodyDescriptor loadClasses(final JsonObject configuration,
      final SoapBodyDescriptor soapBodyDescriptor) {
    try {
      String wsdlUrl = getWsdlUrl(configuration);
      if (isBasicAuth(configuration)) {
        wsdlUrl = loadWsdlLocally(configuration);
      }
      final String binding = Utils.getBinding(configuration);
      final String operation = Utils.getOperation(configuration);
      SoapBodyDescriptor result = soapBodyDescriptor;
      if (null == soapBodyDescriptor) {
        result = JaxbCompiler
            .getSoapBodyDescriptor(wsdlUrl, binding, operation);
        LOGGER.trace("Got SOAP Body Descriptor");
      }
      JaxbCompiler.generateAndLoadJaxbStructure(wsdlUrl);
      return result;
    } catch (NullPointerException npe) {
      LOGGER.error("WSDL URL, Binding and Operation can not be empty");
      throw new ComponentException("WSDL URL, Binding and Operation can not be empty.", npe);
    } catch (Throwable throwable) {
      LOGGER.error("Unexpected error in init method");
      throw new ComponentException(
          String.format("Can not generate Jaxb classes for wsdl. Exception: %s",
              throwable.getMessage()), throwable);
    }
  }

  public static Message callSOAPService(final Message message, final ExecutionParameters parameters,
      final SoapBodyDescriptor soapBodyDescriptor) throws Throwable {
    final JsonObject body = message.getBody();
    final JsonObject configuration = parameters.getConfiguration();
    final SoapCallService soapCallService = new SoapCallService();
    final JsonObject outputBody = soapCallService.call(body, configuration, soapBodyDescriptor);
    return new Message.Builder().body(outputBody).build();
  }

  public static String createSOAPFaultLogString(final SOAPFaultException soapFaultException) {
    return String.format("Server has responded with SOAP fault. Code: %s. Reason: %s",
        soapFaultException.getFault().getFaultCode(),
        soapFaultException.getFault().getFaultString());
  }

  public static void logSOAPMSgIfTraceEnabled(final Logger log, final String message,
      final SOAPMessage soapMessage) throws IOException, SOAPException {
    if (log.isTraceEnabled()) {
      log.trace(message, getStringOfSoapMessage(soapMessage));
    }
  }

  public static String getStringOfSoapMessage(final SOAPMessage soapMessage)
      throws IOException, SOAPException {
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    soapMessage.writeTo(outputStream);
    return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
  }

  public static void configLogger() {
    final org.slf4j.Logger root = LoggerFactory.getLogger("root");
    if (root instanceof ch.qos.logback.classic.Logger) {
      final String level = Optional.ofNullable(System.getenv("LOG_LEVEL")).orElse("info");
      ch.qos.logback.classic.Logger rootLogback = (ch.qos.logback.classic.Logger) root;
      rootLogback.setLevel(Level.toLevel(level));
    }
  }

  /**
   * Extracts SOAP body from envelope and remove namespaces from keys
   *
   * @param body body with envelope
   * @return SOAP body
   */
  public static JsonObject getSoapBody(JsonObject body) {
    final JsonObject envelope = body.getJsonObject(body.keySet().iterator().next());
    final String soapBodyKeyList = envelope.keySet().stream()
        .filter(key -> key.toLowerCase().contains("body"))
        .findFirst()
        .orElseThrow(() -> new ComponentException("SOAP message does not contains SOAP Body"));
    return prettifyBody(envelope.getJsonObject(soapBodyKeyList));
  }

  /**
   * Recursively remove namespace from each JsonObject keys Examples: { { "ns1-a" : "val1",
   *        "a" : "val1", "ns2-b" : {           =========>    "b": { "ns1-c" : {
   * "c" : "val2" "_attr" : ["a, b, c"],          } "_" : "val2" } } } }
   *
   * @param jsonObject json.
   * @return json object with keys without namespace.
   */
  public static JsonObject prettifyBody(final JsonObject jsonObject) {
    try {
      return resolveObject(jsonObject);
    } catch (Exception e) {
      throw new ComponentException(e);
    }
  }

  /**
   * Removes fist part key that is namespace Examples: 1. ns-a -> a 2. ns-a-b -> a-b
   *
   * @param key key with namespace
   * @return key without namespace part.
   */
  private static String getNewKey(final String key) throws Exception {
    final String[] parts = key.split("-");
    final StringBuilder result = new StringBuilder();
    if (parts.length == 1 || parts.length == 0 || key.startsWith("-")) {
      return key;
    }
    for (int i = 0; i < parts.length; i++) {
      if (i == 0) {
        continue;
      }
      result.append(parts[i]).append("-");
    }
    if (key.endsWith("-")) {
      return result.toString();
    }
    return result.deleteCharAt(result.length() - 1).toString();
  }

  /**
   * Remove namespaces from JsonArray
   */
  private static JsonValue resolveArray(JsonArray array) throws Exception {
    Field valueList = array.getClass().getDeclaredField("valueList");
    valueList.setAccessible(true);
    List<JsonValue> newValues = new ArrayList<>();
    Iterator<JsonValue> iter = array.iterator();
    while (iter.hasNext()) {
      JsonValue v = iter.next();
      ValueType type = v.getValueType();
      if (type.equals(ValueType.OBJECT)) {
        JsonObject vo = (JsonObject) v;
        if (vo.containsKey("_")) {
          removeAttributeFromArray(newValues, vo);
          continue;
        }
        newValues.add(resolveObject((JsonObject) vo));
        continue;
      }
      if (type.equals(ValueType.ARRAY)) {
        newValues.add(resolveArray((JsonArray) v));
        continue;
      }
      newValues.add(v);
    }
    valueList.set(array, newValues);
    return array;
  }

  private static void removeAttributeFromArray(List<JsonValue> newValues, JsonObject v)
      throws Exception {
    JsonValue realValue = v.get("_");
    ValueType realType = realValue.getValueType();
    if (realType.equals(ValueType.OBJECT)) {
      newValues.add(resolveObject((JsonObject) realValue));
      return;
    }
    if (realType.equals(ValueType.ARRAY)) {
      newValues.add(resolveArray((JsonArray) realValue));
      return;
    }
    newValues.add(realValue);
  }

  /**
   * Remove namespaces from JsonObject
   */
  private static JsonObject resolveObject(JsonObject object) throws Exception {
    Field valueMap = object.getClass().getDeclaredField("valueMap");
    valueMap.setAccessible(true);
    Map<String, JsonValue> map = (Map<String, JsonValue>) valueMap.get(object);
    Map<String, JsonValue> newKeys = new HashMap<>();
    for (Entry<String, JsonValue> e : map.entrySet()) {
      if (e.getKey().equals("_attr")) {
        continue;
      }
      final String newKey = getNewKey(e.getKey());
      JsonValue value = e.getValue();
      final ValueType type = e.getValue().getValueType();
      if (type.equals(ValueType.OBJECT)) {
        JsonObject v = (JsonObject) value;
        if (v.containsKey("_")) {
          removeAttribute(newKeys, newKey, v);
          continue;
        }
        newKeys.put(newKey, resolveObject((JsonObject) value));
        continue;
      }
      if (type.equals(ValueType.ARRAY)) {
        newKeys.put(newKey, resolveArray((JsonArray) value));
        continue;
      }
      newKeys.put(newKey, value);
    }
    valueMap.set(object, newKeys);
    return object;
  }

  private static void removeAttribute(Map<String, JsonValue> newKeys, String newKey, JsonObject v)
      throws Exception {
    JsonValue realValue = v.get("_");
    ValueType realType = realValue.getValueType();
    if (realType.equals(ValueType.OBJECT)) {
      newKeys.put(newKey, resolveObject((JsonObject) realValue));
      return;
    }
    if (realType.equals(ValueType.ARRAY)) {
      newKeys.put(newKey, resolveArray((JsonArray) realValue));
      return;
    }
    newKeys.put(newKey, realValue);
  }


  public static Document convertStringToXMLDocument(String xmlString) {
    //Parser that produces DOM object trees from XML content
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    //API to obtain DOM Document instance
    DocumentBuilder builder = null;
    try {
      //Create DocumentBuilder with default configuration
      builder = factory.newDocumentBuilder();

      //Parse the content to Document object
      Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
      return doc;
    } catch (Exception e) {
      LOGGER.error("Converting string to XML was not successful");
    }
    return null;
  }
}

