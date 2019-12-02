package io.elastic.soap.jackson;

import com.fasterxml.jackson.databind.JavaType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

public class XmlElementsChoiceDeserializer extends AbstractChoiceDeserializer {


  private final XmlElements annotation;

  public XmlElementsChoiceDeserializer(final JavaType type, final XmlElements annotation) {
    super(type);
    this.annotation = annotation;
  }

  @Override
  public List<Class> getPossibleTypes() {
    return Arrays.stream(this.annotation.value())
        .map(XmlElement::type)
        .filter(c -> !c.equals(XmlElement.DEFAULT.class))
        .collect(Collectors.toList());
  }
}
