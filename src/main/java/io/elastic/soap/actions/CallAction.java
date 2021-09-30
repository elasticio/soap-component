package io.elastic.soap.actions;

import static io.elastic.soap.utils.Utils.callSOAPService;
import static io.elastic.soap.utils.Utils.createSOAPFaultLogString;
import static io.elastic.soap.utils.Utils.loadClasses;

import io.elastic.api.ExecutionParameters;
import io.elastic.api.Function;
import io.elastic.api.InitParameters;
import io.elastic.api.Message;
import io.elastic.api.Message.Builder;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.utils.Utils;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.ws.soap.SOAPFaultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to make a SOAP call.
 */
public class CallAction implements Function {

  static {
    Utils.configLogger();
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(CallAction.class);
  private SoapBodyDescriptor soapBodyDescriptor;

  @Override
  public void init(InitParameters parameters) {
    LOGGER.info("On init started");
    final JsonObject configuration = parameters.getConfiguration();
    soapBodyDescriptor = loadClasses(configuration, soapBodyDescriptor);
    LOGGER.info("On init finished");
  }

  /**
   * Executes the io.elastic.soap.actions's logic by sending a request to the SOAP Service and
   * emitting response to the platform.
   *
   * @param parameters execution parameters
   */
  @Override
  public void execute(final ExecutionParameters parameters) {
    boolean emitSoapFault = Utils.getEmitSoapFault(parameters.getConfiguration());
    try {
      LOGGER.info("Start processing new call to SOAP ");
      final Message message = parameters.getMessage();
      Message data = callSOAPService(message, parameters, soapBodyDescriptor);
      LOGGER.debug("Emitting data...");
      parameters.getEventEmitter().emitData(data);
      LOGGER.info("Finish processing call SOAP action");
    } catch (SOAPFaultException soapFaultException) {
      String exceptionText = createSOAPFaultLogString(soapFaultException);
      LOGGER.error("SOAP Fault occurred");
      if (!emitSoapFault) {
        throw new ComponentException(exceptionText, soapFaultException);
      }
      JsonObjectBuilder jsonObjectBuilder = Utils.buildJsonSoapFault(soapFaultException.getFault());
      JsonObject soapFaultJsonObject = Json.createObjectBuilder().add("fault", jsonObjectBuilder)
          .build();
      Message soapFaultMessage = new Builder().body(soapFaultJsonObject).build();
      parameters.getEventEmitter().emitData(soapFaultMessage);
    } catch (Throwable throwable) {
      LOGGER.error("Unexpected internal component error");
      throw new ComponentException(throwable);
    }
  }
}
