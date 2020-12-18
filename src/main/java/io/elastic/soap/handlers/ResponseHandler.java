package io.elastic.soap.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.exceptions.ComponentException;
import io.elastic.soap.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Class handles XML response and unmarshals it to specified Java type
 */
public class ResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);
    private final String responseBodyElementName;

    public ResponseHandler(String responseBodyElementName) {
        this.responseBodyElementName = responseBodyElementName;
    }

    /**
     * Unmarshalling  {@code response} {@link SOAPMessage} object to {@link T} object
     *
     * @param response {@link SOAPMessage} response from SOAP service
     * @param clazz    The {@link Class} of the {@code response} object
     * @return {@link T} representation of {@link SOAPMessage} {@code response} object
     */
    public <T> T getResponseObject(final SOAPMessage response, final Class<T> clazz)
            throws JAXBException, SOAPException, IOException, SOAPFaultException {

        SOAPFault soapFault = response.getSOAPBody().getFault();
        if (soapFault != null) {
            throw new SOAPFaultException(soapFault);
        }
        LOGGER.info("Start unmarshalling");
        LOGGER.debug("About to start unmarshalling response SoapMessage to {} class", clazz.getName());
        final Unmarshaller unmarshaller = JAXBContext.newInstance(clazz).createUnmarshaller();
        final JAXBElement<T> responseObject = unmarshaller.unmarshal(getResponsePayload(response, clazz), clazz);
        LOGGER.debug("Unmarshalling response SoapMessage to {} class successfully done", clazz.getName());
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
        return jsonObject;
    }

    private Node getResponsePayload(final SOAPMessage response, final Class clazz) throws SOAPException, IOException {
        final int MAX_NUMBER_OF_ITERATIONS = 1_000_000; // Lets prevent infinity loops. In 99% of case getFirstChild will return correct answer,
                                                        // SOAP Body by convention must contain one root element
        LOGGER.trace("Looking for payload with name: {}", responseBodyElementName);
        Node payload = response.getSOAPBody().getFirstChild();
        int i = 0;
        while ( payload != null && !responseBodyElementName.equals(payload.getLocalName()) && i <= MAX_NUMBER_OF_ITERATIONS) {
            i = i + 1;
            LOGGER.trace("Payload have been found yet, checking next sibling. This payload name: {}, number of iterations: {}", payload.getLocalName(), i);
            payload = payload.getNextSibling();
        }
        if (payload == null) {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            response.writeTo(outputStream);
            final String soapBody = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
            throw new ComponentException("Unable to parse response. SOAP response: " + soapBody);
        }
        return payload;
    }

    private String getTargetName(final Class clazz) {
        if (clazz.isAnnotationPresent(XmlRootElement.class)) {
            return ((XmlRootElement) clazz.getAnnotation(XmlRootElement.class)).name();
        }
        if (clazz.isAnnotationPresent(XmlElement.class)) {
            return ((XmlElement) clazz.getAnnotation(XmlElement.class)).name();
        }
        if (clazz.isAnnotationPresent(XmlType.class)) {
            return ((XmlType) clazz.getAnnotation(XmlType.class)).name();
        }
        return clazz.getSimpleName();
    }
}

