package io.elastic.soap.handlers;

import com.thomas_bayer.blz.GetBankType;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequestHandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerTest.class);

    private static JsonObject body;
    private static RequestHandler requestHandler;
    private static SoapBodyDescriptor soapBodyDescriptor;
    private static GetBankType requestObject;

    @BeforeAll
    public static void initConfig() {
        requestHandler = new RequestHandler();
        soapBodyDescriptor = new SoapBodyDescriptor.Builder()
                .setRequestBodyElementName("getBank")
                .setSoapAction("")
                .build();

        requestObject = new GetBankType();
        requestObject.setBlz("67270003");

        body = Json.createReader(new StringReader("{ \"getBank\": { \"blz\": \"67270003\" } }"))
                .readObject();
    }

    @Test
    public void getRequestObjectTest() {
        Class clazz;
        try {
            clazz = Class.forName("com.thomas_bayer.blz.GetBankType");
            final Object requestObject = requestHandler.getRequestObject(body, soapBodyDescriptor, clazz);
            assertEquals(((GetBankType) (requestObject)).getBlz(), "67270003", String
                    .format("expected %s but found actual %s", ((GetBankType) (requestObject)).getBlz(),
                            "67270003"));
        } catch (ClassNotFoundException | IOException e) {
            logger.error("getRequestObjectTest error", e);
        }
    }

    @Test
    public void getSoapRequestMessageTest() {
        Class clazz;
        try {
            clazz = Class.forName("com.thomas_bayer.blz.GetBankType");
            final SOAPMessage soapRequestMessage = requestHandler
                    .getSoapRequestMessage(requestObject, soapBodyDescriptor, clazz);
            assertEquals("getBank", soapRequestMessage.getSOAPBody().getFirstChild().getLocalName(),
                    String.format("expected %s but found actual %s", "getBank",
                            soapRequestMessage.getSOAPBody().getFirstChild().getLocalName()));
            assertEquals("blz",
                    soapRequestMessage.getSOAPBody().getFirstChild().getFirstChild().getLocalName(),
                    String.format("expected %s but found actual %s", "blz",
                            soapRequestMessage.getSOAPBody().getFirstChild().getFirstChild().getLocalName()));
            assertEquals(soapRequestMessage.getSOAPBody().getFirstChild().getFirstChild().getFirstChild()
                    .getNodeValue(), requestObject.getBlz(), String.format("expected %s but found actual %s",
                    soapRequestMessage.getSOAPBody().getFirstChild().getFirstChild().getFirstChild()
                            .getNodeValue(), requestObject.getBlz()));
        } catch (JAXBException | ParserConfigurationException | IOException | SOAPException | ClassNotFoundException e) {
            logger.error("getSoapRequestMessageTest error", e);
        }
    }
}

