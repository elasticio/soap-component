package io.elastic.soap.services;

import io.elastic.soap.AppConstants;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import io.elastic.soap.handlers.RequestHandler;
import io.elastic.soap.handlers.ResponseHandler;
import io.elastic.soap.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;
import java.net.URL;

/**
 * The class which controls all the process of retrieving input data, input configuration, its
 * unmarshalling to Java object and its marshalling to XML afterwards. As well as the opposite
 * process of unmarshalling the returned XML into Java object and marshalling it once again into
 * JSON.
 */
public class SoapCallService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoapCallService.class);

    public JsonObject call(final JsonObject inputJsonObject, final JsonObject configuration, final SoapBodyDescriptor soapBodyDescriptor) throws Throwable {
        final RequestHandler requestHandler = new RequestHandler();
        final Class requestClass = Class.forName(soapBodyDescriptor.getRequestBodyClassName());
        final Object requestObject = requestHandler.getRequestObject(inputJsonObject, soapBodyDescriptor, requestClass);
        final SOAPMessage requestSoapMessage = requestHandler.getSoapRequestMessage(requestObject, soapBodyDescriptor, requestClass);
        final String encodedAuthHeader = Utils.getBasicAuthHeader(configuration);
        requestSoapMessage.getMimeHeaders().addHeader(AppConstants.AUTH_KEYWORD, encodedAuthHeader);
        return callSOAP(requestSoapMessage, soapBodyDescriptor);
    }

    private JsonObject callSOAP(final SOAPMessage requestSoapMessage, final SoapBodyDescriptor soapBodyDescriptor) throws Exception {
        final URL endPoint = new URL(soapBodyDescriptor.getSoapEndPoint());
        final SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
        final SOAPConnection con = factory.createConnection();
        try {
            LOGGER.info("About to start SOAP call...");
            final SOAPMessage response = con.call(requestSoapMessage, endPoint);
            LOGGER.info("SOAP call successfully done");
            final ResponseHandler responseHandler = new ResponseHandler();
            final Class responseClass = Class
                    .forName(soapBodyDescriptor.getResponseBodyClassName());
            final Object responseObject = responseHandler.getResponseObject(response, responseClass);
            return responseHandler
                    .getJsonObject(responseObject, soapBodyDescriptor);
        } finally {
            if (con != null) {
                con.close();
            }
        }
    }
}
