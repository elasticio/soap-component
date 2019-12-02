package io.elastic.soap.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elastic.soap.handlers.RequestHandler;
import io.elastic.soap.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.json.*;
import javax.xml.bind.annotation.XmlElements;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ChoiceMetadataTest {

    @Test
    public void serializeClassWithXmlElementsAnnotation() throws ClassNotFoundException {
        final RequestHandler handler = new RequestHandler();
        final String weatherDescription = "XmlElementsChoice";
        readResourceFileAsJsonArray("choicesElements.json").stream().map(JsonValue::asJsonObject).forEach(o -> {
            System.out.println(o);
            final XmlElementsChoice result = this.wrapAndTest(handler, o, weatherDescription, XmlElementsChoice.class);
            Assertions.assertNotNull(result);
            Assertions.assertNotNull(result.intFieldOrStringFieldOrComplexType);
            final ObjectMapper mapper = Utils.getConfiguredObjectMapper();
            Assertions.assertEquals(getFirstKeyOfJsonObject(o.getJsonObject("XmlElementsChoice")), mapper.convertValue(result, JsonObject.class).get("intFieldOrStringFieldOrComplexType"));
        });
    }

    @Test
    public void serializeClassWithXmlElementRefsAnnotation() throws ClassNotFoundException {
        final RequestHandler handler = new RequestHandler();
        final String weatherDescription = "XmlElementRefsChoice";
        readResourceFileAsJsonArray("choicesRefs.json")
                .stream()
                .map(JsonValue::asJsonObject)
                .forEach(o -> {
                    final XmlElementRefsChoice result = this.wrapAndTest(handler, o, weatherDescription, XmlElementRefsChoice.class);
                    Assertions.assertNotNull(result);
                    Assertions.assertNotNull(result.intFieldOrStringField1OrStringField2);
                    final ObjectMapper mapper = Utils.getConfiguredObjectMapper();
                    Assertions.assertEquals(getFirstKeyOfJsonObject(o.getJsonObject("XmlElementRefsChoice")), mapper.convertValue(result, JsonObject.class).get("intFieldOrStringField1OrStringField2"));
                });
    }

    public <T> T wrapAndTest(RequestHandler handler, JsonObject request, String elementName, Class<T> clazz) {
        try {
            return handler.getObjectFromJson(request, elementName, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JsonArray readResourceFileAsJsonArray(final String path) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        JsonReader jsonReader = Json.createReader(new InputStreamReader(inputStream));
        JsonArray choices = jsonReader.readArray();
        jsonReader.close();
        return choices;
    }
        public JsonValue getFirstKeyOfJsonObject(JsonObject o) {
            return o.values().iterator().next();
        }
}
