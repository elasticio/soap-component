package io.elastic.soap.utils;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.sun.xml.bind.api.impl.NameConverter;
import io.elastic.soap.AppConstants;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;

import io.elastic.soap.exceptions.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {

  private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

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
    final ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

    // Possible features are coming if needed...

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
    LOGGER.info("Got '{}' binding from the config", binding);
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
    LOGGER.info("Got '{}' operation from the config", operation);
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
    final String wsdl = Utils.getConfigParam(config, AppConstants.WSDL_CONFIG_NAME);

    if (!isBasicAuth(config)) {
      return wsdl;
    }
    final String username = Utils.getUsername(config);
    final String password = Utils.getPassword(config);
    return addAuthToURL(wsdl, username, password);
  }

  /**
   * Add auth to URL in form of https://username:password@example.com
   * @param source source of resource.
   * @param username username.
   * @param password password.
   * @return URI with username and password.
   */
  public static String addAuthToURL(final String source, final String username, final String password) {
    try {
      final URI uri = new URI(source);
      return new URI(uri.getScheme(), username + ":" + password, uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), uri.getFragment()).toString();
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
    boolean isBasicAuth = false;
    if (AppConstants.BASIC_AUTH_CONFIG_NAME.equals(authType)) {
      isBasicAuth = true;
    }
    return isBasicAuth;
  }

  /**
   * Builds basic auth header for authorization. E.g.: Basic X0VJT0VYVEVOREVEOldlbGNvbWUxMjM=
   */
  public static String getBasicAuthHeader(JsonObject config) {
    return Base64Utils.getBasicAuthHeader(config);
  }

  /**
   * Retrieves username from the credentials object
   */
  protected static String getUsername(JsonObject config) {
    return config.getJsonObject("auth").getJsonObject("basic").getString("username");
  }

  /**
   * Retrieves password from the credentials object
   */
  protected static String getPassword(JsonObject config) {
    return config.getJsonObject("auth").getJsonObject("basic").getString("password");
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
}

