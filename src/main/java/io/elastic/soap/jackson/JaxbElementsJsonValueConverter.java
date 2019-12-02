package io.elastic.soap.jackson;

import com.fasterxml.jackson.databind.util.StdConverter;
import io.elastic.soap.utils.Utils;

import javax.json.JsonValue;
import javax.xml.bind.JAXBElement;
import java.util.List;

public class JaxbElementsJsonValueConverter extends StdConverter<List<? extends JAXBElement>, JsonValue> {

    /**
     *
     * @param value hack to skip check for JaxbElements coonverting
     * @return list of real values.
     */
    @Override
    public JsonValue convert(List<? extends JAXBElement> value) {
        List rawList = value;
        return Utils.getConfiguredObjectMapper().convertValue(rawList, JsonValue.class);
    }
}
