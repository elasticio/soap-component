package io.elastic.soap.handlers;

import com.thomas_bayer.blz.DetailsType;
import com.thomas_bayer.blz.GetBankResponseType;
import io.elastic.soap.compilers.model.SoapBodyDescriptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.json.JsonObject;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseHandlerTest {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandlerTest.class);


    private static SOAPMessage responseMessage;
    private static ResponseHandler responseHandler;
    private static SoapBodyDescriptor soapBodyDescriptor;
    private static GetBankResponseType responseObject;

    @BeforeAll
    public static void initConfig() throws SOAPException {
        responseHandler = new ResponseHandler("getBankResponse");
        soapBodyDescriptor = new SoapBodyDescriptor.Builder()
                .setResponseBodyElementName("getBankResponse")
                .build();

        final MessageFactory factory = MessageFactory.newInstance();
        responseMessage = factory.createMessage();
        final SOAPPart part = responseMessage.getSOAPPart();
        final SOAPEnvelope envelope = part.getEnvelope();
        final SOAPBody body = envelope.getBody();

        final SOAPBodyElement getBankResponse = body.addBodyElement(
                envelope.createName("getBankResponse", "ns1", "http://thomas-bayer.com/blz/"));
        final SOAPElement details = getBankResponse.addChildElement("details");
        details.addChildElement(new QName("http://thomas-bayer.com/blz/", "bezeichnung", "ns1"))
                .addTextNode("Deutsche Bank");
        details.addChildElement(new QName("http://thomas-bayer.com/blz/", "bic", "ns1"))
                .addTextNode("DEUTDESM672");
        details.addChildElement(new QName("http://thomas-bayer.com/blz/", "ort", "ns1"))
                .addTextNode("Heidelberg, Neckar");
        details.addChildElement(new QName("http://thomas-bayer.com/blz/", "plz", "ns1"))
                .addTextNode("69111");

        responseObject = new GetBankResponseType();
        final DetailsType detailsType = new DetailsType();
        detailsType.setBezeichnung("Deutsche Bank");
        detailsType.setBic("DEUTDESM672");
        detailsType.setOrt("Heidelberg, Neckar");
        detailsType.setPlz("69111");
        responseObject.setDetails(detailsType);
    }

    @Test
    public void getResponseObjectTest() {
        Class clazz;
        try {
            clazz = Class.forName("com.thomas_bayer.blz.GetBankResponseType");
            final GetBankResponseType responseObject = (GetBankResponseType) responseHandler
                    .getResponseObject(responseMessage, clazz);
            final DetailsType details = responseObject.getDetails();

            /*structure asserts*/
            assertEquals("getBankResponse",
                    responseMessage.getSOAPBody().getFirstChild().getLocalName(), String
                            .format("expected is %s but actual is %s", "getBankResponse",
                                    responseMessage.getSOAPBody().getFirstChild().getLocalName()));
            assertEquals("details",
                    responseMessage.getSOAPBody().getFirstChild().getFirstChild().getLocalName(), String
                            .format("expected is %s but actual is %s", "details",
                                    responseMessage.getSOAPBody().getFirstChild().getFirstChild().getLocalName()));
            assertEquals("bezeichnung",
                    responseMessage.getSOAPBody().getFirstChild().getFirstChild().getFirstChild()
                            .getLocalName(), String
                            .format("expected is %s but actual is %s", "bezeichnung",
                                    responseMessage.getSOAPBody().getFirstChild().getFirstChild().getFirstChild()
                                            .getLocalName()));
        } catch (ClassNotFoundException | IOException | SOAPException | JAXBException e) {
            logger.error("getResponseObjectTest error", e);
        }
    }

    @Test
    public void getJsonObjectTest() {
            final JsonObject responseJsonObject = responseHandler
                    .getJsonObject(responseObject, soapBodyDescriptor);
            final JsonObject details = responseJsonObject.getJsonObject("getBankResponse")
                    .getJsonObject("details");
            final DetailsType detailsObject = responseObject.getDetails();
            assertEquals(detailsObject.getBezeichnung(), details.getString("bezeichnung"),
                    String.format("expected is %s but actual is %s", detailsObject.getBezeichnung(),
                            details.getString("bezeichnung")));
            assertEquals(detailsObject.getBic(), details.getString("bic"),
                    String.format("expected is %s but actual is %s", detailsObject.getBic(),
                            details.getString("bic")));
            assertEquals(detailsObject.getOrt(), details.getString("ort"),
                    String.format("expected is %s but actual is %s", detailsObject.getOrt(),
                            details.getString("ort")));
            assertEquals(detailsObject.getPlz(), details.getString("plz"),
                    String.format("expected is %s but actual is %s", detailsObject.getPlz(),
                            details.getString("plz")));
    }
}

