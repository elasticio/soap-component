package io.elastic.soap.triggers;

import static io.elastic.soap.AppConstants.VALIDATION;
import static io.elastic.soap.AppConstants.VALIDATION_ENABLED;
import static io.elastic.soap.utils.Utils.configLogger;
import static io.elastic.soap.utils.Utils.loadClasses;

import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.InitParameters;
import io.elastic.api.Message;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.utils.Utils;
import io.elastic.soap.validation.SOAPValidator;
import io.elastic.soap.validation.ValidationResult;
import io.elastic.soap.validation.impl.WsdlSOAPValidator;
import javax.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Trigger to get soap request.
 */
public class ReceiveRequest implements Function {

  static {
    configLogger();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveRequest.class);

  private SOAPValidator validator;

  private SoapBodyDescriptor soapBodyDescriptor;

  @Override
  public void init(InitParameters parameters) {
    try {
      LOGGER.info("On init started");
      final JsonObject configuration = parameters.getConfiguration();
      soapBodyDescriptor = loadClasses(configuration, soapBodyDescriptor);
      validator = new WsdlSOAPValidator(soapBodyDescriptor.getRequestBodyClassName());
      LOGGER.info("On init finished");
    } catch (ComponentException e) {
      LOGGER.error("Error in init method");
      throw e;
    } catch (Exception e) {
      LOGGER.error("Error in init method");
      throw new ComponentException(e);
    }
  }

  /**
   * @param parameters execution parameters
   */
  @Override
  public void execute(final ExecutionParameters parameters) {
    try {
      final Message message = parameters.getMessage();
      final JsonObject body1 = message.getBody();
      final JsonObject query = message.getQuery();
      final String method = message.getMethod();
      final String url = message.getUrl();
      final String originalUrl = message.getOriginalUrl();

      LOGGER.info("Entire message: " + message);
      LOGGER.info("body: " + body1.toString());
      LOGGER.info("query: " + query.toString());
      LOGGER.info("method: " + method);
      LOGGER.info("url: " + url);
      LOGGER.info("originalUrl: " + originalUrl);
      final JsonObject configuration = parameters.getConfiguration();
      final JsonObject body = Utils.getSoapBody(message.getBody());
      LOGGER.info("Received new SOAP message, start processing");
      final Message data = new Message.Builder().body(body).build();
      if (VALIDATION_ENABLED.equals(configuration.getString(VALIDATION, VALIDATION_ENABLED))) {
        LOGGER.trace("Validation is required for SOAP message");
        final JsonObject content = (JsonObject) body.values().toArray()[0];
        final ValidationResult validationResult = validator.validate(content);
        if (!validationResult.isResult()) {
         throw validationResult.getException();
        }
      }
      LOGGER.debug("Emitting data...");
      parameters.getEventEmitter().emitData(data);
      LOGGER.info("Finished processing SOAP message");
    } catch (ComponentException e) {
      LOGGER.error("Error in receive request trigger");
      throw e;
    } catch (Exception e) {
      LOGGER.error("Error in receive request trigger");
      throw new ComponentException(e);
    }
  }

  /**
   * For unit testing
   */
  public void setValidator(SOAPValidator validator) {
    this.validator = validator;
  }

}
