package io.elastic.soap.providers;

import com.predic8.wsdl.Binding;
import com.predic8.wsdl.Definitions;
import com.predic8.wsdl.WSDLParser;
import io.elastic.api.JSON;
import io.elastic.api.SelectModelProvider;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.List;

import static io.elastic.soap.AppConstants.SOAP11_PROTOCOL_NAME;
import static io.elastic.soap.AppConstants.SOAP12_PROTOCOL_NAME;

/**
 * Provides data for input Binding select box.
 * It is a list of bindings available in the provided WSDL
 */
public class BindingModelProvider implements SelectModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(BindingModelProvider.class);

    @Override
    public JsonObject getSelectModel(final JsonObject configuration) {
        try {
            LOGGER.info("Input model configuration: {}", JSON.stringify(configuration));
            final String wsdlUrl = Utils.getWsdlUrl(configuration);
            final List<Binding> bindings = getDefinitionsFromWsdl(wsdlUrl).getBindings();
            final JsonObjectBuilder builder = Json.createObjectBuilder();
            bindings.stream()
                    .filter(this::isSupportedSOAPVersion)
                    .forEach(b -> builder.add(b.getName(), b.getName()));
            final JsonObject result = builder.build();
            if (result.keySet().size() == 0) {
                throw new ComponentException(String.format("All bindings in have unsupported SOAP protocol version, supported versions: [%s, %s]",
                        SOAP11_PROTOCOL_NAME,
                        SOAP12_PROTOCOL_NAME));
            }
            LOGGER.trace("Result bindings list {}", result);
            LOGGER.info("Finish creating bindings list");
            return result;
        } catch (ComponentException e) {
            LOGGER.error("Exception while creating bindings list for component", e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unexpected exception while creating bindings list for component", e);
            throw new ComponentException("Unexpected exception while creating bindings list for component", e);
        }
    }

    /**
     * Method calls external WSDL by its URL and parses it
     *
     * @return {@link Definitions} object
     */
    public Definitions getDefinitionsFromWsdl(final String wsdlUrl) {
        final WSDLParser parser = new WSDLParser();
        return parser.parse(wsdlUrl);
    }

    private boolean isSupportedSOAPVersion(final Binding binding) {
        final Object version = binding.getProtocol();
        return SOAP11_PROTOCOL_NAME.equals(version) || SOAP12_PROTOCOL_NAME.equals(version);
    }
}
