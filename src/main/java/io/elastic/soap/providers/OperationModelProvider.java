package io.elastic.soap.providers;

import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import io.elastic.api.JSON;
import io.elastic.api.SelectModelProvider;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.services.WSDLService;
import io.elastic.soap.services.impls.HttpWSDLService;
import io.elastic.soap.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Provides data for input Operation select box.
 * It is a list of operations available in the provided WSDL for a given Binding
 */

public class OperationModelProvider implements SelectModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationModelProvider.class);
    private WSDLService wsdlService = new HttpWSDLService();

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        try {
            LOGGER.info("Input model configuration: {}", JSON.stringify(configuration));
            final String bindingName = Utils.getBinding(configuration);
            final String wsdlUrl = Utils.getWsdlUrl(configuration);
            final Definitions wsdl = wsdlService.getWSDL(configuration);
            final Binding binding = wsdl.getBinding(bindingName);
            final JsonObjectBuilder builder = Json.createObjectBuilder();
            binding.getOperations().forEach(o -> builder.add(o.getName(), o.getName()));
            final JsonObject result = builder.build();
            if (result.keySet().size() == 0) {
                throw new ComponentException(String.format("No operations where found for binding: %s, in wsdl.", bindingName));
            }
            LOGGER.trace("Result operation list {}", result);
            LOGGER.info("Finish creating operation list");
            LOGGER.info("Input wsdl url: {}, binding: {}", wsdlUrl, bindingName);
            return result;
        } catch (ComponentException e) {
            LOGGER.error("Exception while creating operation list for component", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected exception while creating operation list for component", e);
            throw new ComponentException("Unexpected exception while creating operation list for component", e);
        }
    }

    public WSDLService getWsdlService() {
        return wsdlService;
    }

    public void setWsdlService(final WSDLService wsdlService) {
        this.wsdlService = wsdlService;
}

}
