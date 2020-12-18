package io.elastic.soap.actions;

import static io.elastic.soap.AppConstants.VALIDATION;
import static io.elastic.soap.AppConstants.VALIDATION_DISABLED;
import static io.elastic.soap.AppConstants.VALIDATION_ENABLED;
import static io.elastic.soap.utils.Utils.loadClasses;

import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.HttpReply;
import io.elastic.api.InitParameters;
import io.elastic.api.Message;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.utils.Utils;
import io.elastic.soap.validation.SOAPValidator;
import io.elastic.soap.validation.ValidationResult;
import io.elastic.soap.validation.impl.WsdlSOAPValidator;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class SoapReplyAction implements Function {

  static {
    Utils.configLogger();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(SoapReplyAction.class);

  private SOAPValidator validator;

  public static final String HEADER_CONTENT_TYPE = "Content-Type";
  public static final String HEADER_ROUTING_KEY = "X-EIO-Routing-Key";
  public static final String CONTENT_TYPE = "text/xml";
  private SoapBodyDescriptor soapBodyDescriptor;

  @Override
  public void init(InitParameters parameters) {
    try {
      LOGGER.info("On init started");
      final JsonObject configuration = parameters.getConfiguration();
      soapBodyDescriptor = loadClasses(configuration, soapBodyDescriptor);
      validator = new WsdlSOAPValidator(soapBodyDescriptor.getResponseBodyClassName());
      LOGGER.info("On init finished");
    } catch (ComponentException e) {
      LOGGER.error("Error occurred in init method");
      throw e;
    } catch (Exception e) {
      LOGGER.error("Error occurred in init method");
      throw new ComponentException(e);
    }
  }

  @Override
  public void execute(ExecutionParameters parameters) {
    try {
      final JsonObject configuration = parameters.getConfiguration();
      final Message inputMsg = parameters.getMessage();
      final JsonObject body = inputMsg.getBody();

      final boolean enableValidation = !VALIDATION_ENABLED.equals(configuration.getString(VALIDATION, VALIDATION_DISABLED));

      final Document document;
      final String xmlString;
      if (enableValidation) {
        LOGGER.trace("Validation is required for SOAP message");
        ValidationResult validationResult = validator
            .validate(body.getJsonObject(body.keySet().iterator().next()));
        document = validationResult.getResultXml();
        xmlString = validationResult.getXmlString();
      } else {
        xmlString = XML.toString(new JSONObject(body.toString()));
        document = Utils.convertStringToXMLDocument(xmlString);
      }
      String replyTo = inputMsg.getHeaders().get("reply_to") != null ? inputMsg.getHeaders()
          .getString("reply_to") : null;

      // Don't emit this message when running sample data
      LOGGER.info("Creating output message...");
      if (null == replyTo) {
        LOGGER.error("No reply_to id found!");
        return;
      }

      final SOAPMessage message = MessageFactory.newInstance().createMessage();
      final MimeHeaders soapHeaders = message.getMimeHeaders();

      soapHeaders.addHeader("SOAPAction", soapBodyDescriptor.getSoapAction());
      message.getSOAPBody().addDocument(document);

      if (enableValidation) {
        message.getSOAPBody()
            .addAttribute(new QName("xmlns"), soapBodyDescriptor.getResponseBodyNameSpace());
      }

      String soapResponseMessageString = enableValidation
          ? Utils.getStringOfSoapMessage(message)
          : Utils.getStringOfSoapMessage(message).replaceAll(" xmlns=\"\"", "");

      LOGGER.info("Building HTTP reply object...");
      InputStream in = new ByteArrayInputStream(soapResponseMessageString.getBytes());
      HttpReply httpReply = new HttpReply.Builder()
          .content(in)
          .header(HEADER_ROUTING_KEY, replyTo)
          .header(HEADER_CONTENT_TYPE, CONTENT_TYPE)
          .status(200)
          .build();

      LOGGER.info("Making HTTP reply...");
      parameters.getEventEmitter().emitHttpReply(httpReply);

      final JsonObject soapResponse = Json.createObjectBuilder()
          .add("SoapResponse", soapResponseMessageString)
          .build();

      LOGGER.info("Emitting data...");
      parameters.getEventEmitter().emitData(new Message.Builder().body(soapResponse).build());
    } catch (ComponentException e) {
      LOGGER.error("Got component exception");
      parameters.getEventEmitter().emitException(e);
    } catch (Exception e) {
      LOGGER.error("Error occurred");
      parameters.getEventEmitter().emitException(e);
    }
  }
}
