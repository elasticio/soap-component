package io.elastic.soap.actions;

import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import io.elastic.soap.services.SoapCallService;
import javax.json.JsonObject;
import javax.xml.ws.soap.SOAPFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to make a SOAP call.
 */
public class CallAction implements Module {

  private static final Logger LOGGER = LoggerFactory.getLogger(CallAction.class);

  @Override
  public void init(JsonObject configuration) {

  }

  /**
   * Executes the io.elastic.soap.actions's logic by sending a request to the SOAP Service and
   * emitting response to the platform.
   *
   * @param parameters execution parameters
   */
  @Override
  public void execute(final ExecutionParameters parameters) {

    Message data;
    final Message message = parameters.getMessage();
    LOGGER.info("Input message: {}", message);

    final JsonObject body = message.getBody();
    final JsonObject configuration = parameters.getConfiguration();

    JsonObject outputBody = null;
    final SoapCallService soapCallService = new SoapCallService();
    try {
      outputBody = soapCallService.call(body, configuration);
    } catch (SOAPFaultException soapFaultException) {
      String soapFaultCode = soapFaultException.getFault().getFaultCode();
      String soapFaultString = soapFaultException.getFault().getFaultString();
      String exceptionText =
          "Server has responded with SOAP fault. See logs for more details. Code: "
              + soapFaultCode + ". Reason: " + soapFaultString + ".";
      LOGGER.error(exceptionText);

      // throwing an exception
      throw new RuntimeException(exceptionText);
    } catch (Throwable throwable) {
      LOGGER.error("Unexpected internal component error: {}", throwable.getMessage());
      throw new RuntimeException(throwable);
    }

    data = new Message.Builder().body(outputBody).build();
    LOGGER.info("Emitting data: {}", outputBody);

    // emitting the message to the platform
    parameters.getEventEmitter().emitData(data);
  }
}
