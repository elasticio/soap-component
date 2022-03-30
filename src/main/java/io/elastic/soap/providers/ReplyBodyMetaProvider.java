package io.elastic.soap.providers;

import static io.elastic.soap.providers.BodyMetaProvider.deepRemoveKey;
import static io.elastic.soap.providers.BodyMetaProvider.deepRemoveNull;
import static io.elastic.soap.utils.Utils.getElementName;
import static io.elastic.soap.utils.Utils.isBasicAuth;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.Message;
import com.predic8.wsdl.Operation;
import io.elastic.api.DynamicMetadataProvider;
import io.elastic.soap.AppConstants;
import io.elastic.soap.compilers.JaxbCompiler;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.services.WSDLService;
import io.elastic.soap.services.impls.HttpWSDLService;
import io.elastic.soap.utils.Utils;
import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Provides dynamically generated fields set representing correlated XSD schema for given WSDL, its binding and operation.
 */
public class ReplyBodyMetaProvider implements DynamicMetadataProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReplyBodyMetaProvider.class);
  private WSDLService wsdlService = new HttpWSDLService();
  private static final JsonNodeFactory factory = JsonNodeFactory.instance;

  private JsonObject generateSchema(final Message message) throws ComponentException {
    try {
      final ObjectMapper objectMapper = Utils.getConfiguredObjectMapper();
      final String elementName = getElementName(message);
      final String className;
      try {
        className = JaxbCompiler.getClassName(message, elementName, "", "");
      } catch (ParserConfigurationException e) {
        LOGGER.error("Could not map the Json to deserialize schema");
        throw new ComponentException("Could not map the Json to deserialize schema", e);
      } catch (XPathExpressionException e) {
        LOGGER.error("Could not map the Json to deserialize schema");
        throw new ComponentException("Could not map the Json to deserialize schema", e);
      } catch (IOException e) {
        LOGGER.error("Could not map the Json to deserialize schema");
        throw new ComponentException("Could not map the Json to deserialize schema", e);
      } catch (SAXException e) {
        LOGGER.error("Could not map the Json to deserialize schema");
        throw new ComponentException("Could not map the Json to deserialize schema", e);
      }
      final JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(objectMapper);
      final ObjectNode schema = objectMapper.valueToTree(schemaGen.generateSchema(Class.forName(className)));
      final ObjectNode properties = (ObjectNode) schema.get("properties");
      final ObjectNode propertiesType = factory.objectNode();
      propertiesType.set("type", factory.textNode("object"));
      propertiesType.set("properties", properties);
      final JsonNode classNameNode = factory.objectNode().set(elementName, propertiesType);
      final JsonNode result = schema.set("properties", classNameNode);
      deepRemoveKey(result.fields(), "id");
      deepRemoveNull(result.fields());
      return objectMapper.convertValue(result, JsonObject.class);
    } catch (JsonMappingException e) {
      LOGGER.error("Could not map the Json to deserialize schema");
      throw new ComponentException("Could not map the Json to deserialize schema", e);
    } catch (ClassNotFoundException e) {
      LOGGER.error("The class in the schema can not be found");
      throw new ComponentException("The class in the schema can not be found", e);
    }
  }

  @Override
  public JsonObject getMetaModel(final JsonObject configuration) {
    try {
      LOGGER.info("Start creating meta data for component");
      String wsdlUrl = Utils.getWsdlUrl(configuration);
      if (Utils.isBasicAuth(configuration)) {
        final String username = Utils.getUsername(configuration);
        final String password = Utils.getPassword(configuration);
        wsdlUrl = Utils.addAuthToURL(wsdlUrl, username, password);
      }
      final String bindingName = Utils.getBinding(configuration);
      final String operationName = Utils.getOperation(configuration);
      final Definitions wsdl = wsdlService.getWSDL(configuration);
      JaxbCompiler.generateAndLoadJaxbStructure(wsdlUrl);
      final String portTypeName = wsdl.getBinding(bindingName).getPortType().getName();
      final Operation operation = wsdl.getOperation(operationName, portTypeName);
      final JsonObject out = generateSchema(operation.getOutput().getMessage());
      final JsonObject result = Json.createObjectBuilder()
          .add("in", out)
          .add("out", out)
          .build();
      LOGGER.info("Successfully generated component metadata");
      return result;
    } catch (ComponentException e) {
      throw e;
    } catch (Exception e) {
      LOGGER.error("Unexpected exception while creating metadata for component");
      throw new ComponentException("Unexpected exception while creating metadata for component", e);
    } catch (Throwable throwable) {
      LOGGER.error("Unexpected exception while creating metadata for component");
      throw new ComponentException("Unexpected error while creating metadata for component", throwable);
    }
  }

  public WSDLService getWsdlService() {
    return wsdlService;
  }

  public void setWsdlService(final WSDLService wsdlService) {
    this.wsdlService = wsdlService;
  }
}
