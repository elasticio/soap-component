package io.elastic.soap.validation.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.exceptions.ValidationException;
import io.elastic.soap.utils.Utils;
import io.elastic.soap.validation.SOAPValidator;
import io.elastic.soap.validation.ValidationResult;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import javax.json.JsonObject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.input.ReaderInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Validate SOAP message over WSDL.
 */
public class WsdlSOAPValidator extends SOAPValidator {

  private static final Logger LOGGER = LoggerFactory.getLogger(WsdlSOAPValidator.class);

  public WsdlSOAPValidator(final String clazzName) throws ClassNotFoundException {
    super(clazzName);
  }

  /**
   * @param message input message that validated by validator, usually received from action/trigger
   * @return result of validation
   */
  @Override
  public ValidationResult validate(final JsonObject message) {
    try {
      LOGGER.trace("Starting message validation");
      final ObjectMapper mapper = Utils.getConfiguredObjectMapper();
      Object mappingResult = mapper.readValue(message.toString(), clazz);

      JAXBContext jaxbContext = JAXBContext.newInstance(mappingResult.getClass());
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      StringWriter sw = new StringWriter();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();

      marshaller.marshal(mappingResult, sw);

      String xmlString = sw.toString();

      InputStream is = new ReaderInputStream(new StringReader(xmlString));

      Document doc = builder.parse(is);

      LOGGER.trace("Successful finished message validation");
      return new ValidationResult(xmlString, doc);
    } catch (JsonParseException | JsonMappingException e) {
      LOGGER.error("Failed to validate message");
      return new ValidationResult(new ValidationException(e.getLocation().toString(), e));
    } catch (Exception e) {
      LOGGER.error("Failed to validate message");
      throw new ComponentException(e);
    }
  }
}
