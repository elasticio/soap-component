package io.elastic.soap.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.elastic.api.DynamicMetadataProvider;
import io.elastic.soap.compilers.JaxbCompiler;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.services.WSDLService;
import io.elastic.soap.services.impls.HttpWSDLService;
import io.elastic.soap.utils.Utils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides dynamically generated fields set representing correlated XSD schema for given WSDL, its
 * binding and operation.
 */
public class BodyMetaProvider implements DynamicMetadataProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(BodyMetaProvider.class);
  private WSDLService wsdlService = new HttpWSDLService();

  private JsonSchema inSchema;
  private JsonSchema outSchema;
  private JsonElement out, in;

  public byte[] getMeta(final JsonObject configuration) throws Throwable {

    String wsdlUrl;
    String binding;
    String operation;

    try {
      wsdlUrl = Utils.addAuthToURL(Utils.getWsdlUrl(configuration), Utils.getUsername(configuration), Utils.getPassword(configuration));
      binding = Utils.getBinding(configuration);
      operation = Utils.getOperation(configuration);
    } catch (NullPointerException npe) {
      LOGGER.error("WSDL URL, Binding and Operation can not be empty.");
      throw new RuntimeException(npe);
    }
    LOGGER.info("Got configuration: WSDL URL: {}, Binding: {}, Operation: {}", wsdlUrl, binding,
        operation);
    final SoapBodyDescriptor soapBodyDescriptor = getSoapBodyDescriptor(wsdlUrl, binding,
            operation);

    LOGGER.info("Received SOAP Body descriptor: {}", soapBodyDescriptor);

    try {
      JaxbCompiler.generateAndLoadJaxbStructure(wsdlUrl);
    } catch (Throwable throwable) {
      LOGGER.error("Internal JAXB structure was failed to generate. Check logs");
      throw new RuntimeException(throwable);
    }

    final String className = soapBodyDescriptor.getRequestBodyClassName();
    if (className == null) {
      throw new RuntimeException(
          "Either Element Name or Class Name (Type) of the XSD element MUST be not empty. "
              + "Validate an XSD schema and try again.");
    }
    LOGGER.info("ClassName for the root element of the {} operation is {}", operation, className);
    return generateSchema(soapBodyDescriptor).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Class that calls external WSDL, parses it and generates JAXB structure, loading it into
   * classloader
   */
  public SoapBodyDescriptor getSoapBodyDescriptor(final String wsdlUrl, final String binding,
      final String operation) {
    return JaxbCompiler.getSoapBodyDescriptor(wsdlUrl, binding, operation);
  }

  private String generateSchema(final SoapBodyDescriptor soapBodyDescriptor)
      throws JsonProcessingException {
    final ObjectMapper objectMapper = Utils.getConfiguredObjectMapper();
    String resultSchema = null;
    try {
      final JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(objectMapper);
      inSchema = schemaGen.generateSchema(Class.forName(soapBodyDescriptor.getRequestBodyClassName()));
      outSchema = schemaGen.generateSchema(Class.forName(soapBodyDescriptor.getResponseBodyClassName()));
      resultSchema = appendToSchemaStructure(soapBodyDescriptor).toString();
      LOGGER.info("Generated schema: {}", resultSchema);
    } catch (JsonMappingException e) {
      LOGGER.error("Could not map the Json to deserialize schema");
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      LOGGER.error("The class in the schema can not be found");
      throw new RuntimeException(e);
    }
    return resultSchema;
  }


  private com.google.gson.JsonObject appendToSchemaStructure(SoapBodyDescriptor soapBodyDescriptor) throws JsonProcessingException {
    ObjectMapper objectMapper = Utils.getConfiguredObjectMapper();
    final String rootStr = "{\"type\":\"object\",\"properties\":{}}";
    final String inSchemaString = objectMapper.writeValueAsString(inSchema);
    final String outSchemaString = objectMapper.writeValueAsString(outSchema);
    final JsonParser parser = new JsonParser();

    in = parser.parse(rootStr);
    com.google.gson.JsonObject inProps = in.getAsJsonObject().getAsJsonObject("properties");
    out = parser.parse(rootStr);
    com.google.gson.JsonObject outProps = out.getAsJsonObject().getAsJsonObject("properties");

    inProps.add(soapBodyDescriptor.getRequestBodyElementName(), parser.parse(inSchemaString));
    outProps.getAsJsonObject().add(soapBodyDescriptor.getResponseBodyElementName(), parser.parse(outSchemaString));
    final com.google.gson.JsonObject gsonInOutSchema = new com.google.gson.JsonObject();
    gsonInOutSchema.add("in", in);
    gsonInOutSchema.add("out", out);
    return gsonInOutSchema;
  }

  @Override
  public JsonObject getMetaModel(final JsonObject configuration) {
    try {
      LOGGER.info("Got configuration: {}", configuration.toString());
      return deserialize(getMeta(configuration));
    } catch (IOException e) {
      LOGGER.error("IOException caught. Check the logs");
      throw new RuntimeException(e);
    } catch (Throwable throwable) {
      LOGGER.error("Throwable caught. Check the logs");
      throw new RuntimeException(throwable);
    }
  }

  public JsonObject deserialize(final byte[] content) throws IOException {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(content);
        JsonReader reader = Json.createReader(bais)) {
      return reader.readObject();
    }
  }

  public WSDLService getWsdlService() {
    return wsdlService;
  }

  public void setWsdlService(final WSDLService wsdlService) {
    this.wsdlService = wsdlService;
  }
}
