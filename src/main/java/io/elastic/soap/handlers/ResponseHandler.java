package io.elastic.soap.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.IOException;

/**
 * Class handles XML response and unmarshals it to specified Java type
 */
public class ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);

    /**
     * Unmarshalling  {@code response} {@link SOAPMessage} object to {@link T} object
     *
     * @param response {@link SOAPMessage} response from SOAP service
     * @param clazz    The {@link Class} of the {@code response} object
     * @return {@link T} representation of {@link SOAPMessage} {@code response} object
     */
    public <T> T getResponseObject(final SOAPMessage response, final Class<T> clazz)
            throws JAXBException, SOAPException, IOException, SOAPFaultException {
        Utils.logSOAPMSgIfTraceEnabled(LOGGER, "Response SOAP message: {}", response);

        SOAPFault soapFault = response.getSOAPBody().getFault();
        if (soapFault != null) {
            throw new SOAPFaultException(soapFault);
        }
        LOGGER.info("Start unmarshalling");
        LOGGER.trace("About to start unmarshalling response SoapMessage to {} class", clazz.getName());
        final Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();

        final JAXBElement<T> responseObject = unmarshaller.unmarshal(response.getSOAPBody().getFirstChild(), clazz);
        LOGGER.trace("Unmarshalling response SoapMessage to {} class successfully done", clazz.getName());
        LOGGER.info("Finish unmarshalling");
        return responseObject.getValue();
    }

    /**
     * Deserialization {@code response} {@link JsonObject} to {@link Object} object
     *
     * @param response Java {@link Object} representation of SOAP response structure
     * @return {@link JsonObject} representation of {@link Object} {@code request} object
     */
    public JsonObject getJsonObject(final Object response,
                                    final SoapBodyDescriptor soapBodyDescriptor) {
        LOGGER.info("About to start serialization response SoapMessage to JsonObject");
        final ObjectMapper mapper = Utils.getConfiguredObjectMapper();
        final JsonObject jsonResponseObject = mapper.convertValue(response, JsonObject.class);

        final JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(soapBodyDescriptor.getResponseBodyElementName(), jsonResponseObject);
        final JsonObject jsonObject = builder.build();
        LOGGER.info("JSON object successfully serialized");
        LOGGER.trace("JSON object: {}", jsonObject);
        return jsonObject;
    }
}

