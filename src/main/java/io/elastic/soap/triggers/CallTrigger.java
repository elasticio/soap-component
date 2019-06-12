package io.elastic.soap.triggers;

import io.elastic.api.ExecutionParameters;
import io.elastic.api.Message;
import io.elastic.api.Module;
import io.elastic.soap.actions.CallAction;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.services.SoapCallService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.xml.ws.soap.SOAPFaultException;

import static io.elastic.soap.utils.Utils.callSOAPService;
import static io.elastic.soap.utils.Utils.createSOAPFaultLogString;
import static io.elastic.soap.utils.Utils.loadClasses;

/**
 * Trigger to get pets by status.
 */
public class CallTrigger implements Module {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallAction.class);
    private SoapBodyDescriptor soapBodyDescriptor;

    @Override
    public void init(JsonObject configuration) {
        LOGGER.info("On init started");
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
        try {
            LOGGER.info("Start processing new call to SOAP trigger");
            final Message message = parameters.getMessage();
            LOGGER.trace("Input message: {}", message);
            Message data = callSOAPService(message, parameters, soapBodyDescriptor);
            LOGGER.trace("Emitting data: {}", data);
            parameters.getEventEmitter().emitData(data);
            LOGGER.info("Finish processing call to SOAP trigger");
        } catch (SOAPFaultException soapFaultException) {
            String exceptionText = createSOAPFaultLogString(soapFaultException);
            LOGGER.error(exceptionText, soapFaultException);
            throw new ComponentException(exceptionText, soapFaultException);
        } catch (Throwable throwable) {
            LOGGER.error("Unexpected internal component error.", throwable);
            throw new ComponentException(throwable);
        }
    }
}
