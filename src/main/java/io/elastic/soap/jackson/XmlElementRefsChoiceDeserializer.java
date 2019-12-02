package io.elastic.soap.jackson;

import com.fasterxml.jackson.databind.JavaType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;

public class XmlElementRefsChoiceDeserializer extends AbstractChoiceDeserializer {

  private final XmlElementRefs annotation;

  public XmlElementRefsChoiceDeserializer(final JavaType type, final XmlElementRefs annotation) {
    super(type);
    this.annotation = annotation;
  }

  @Override
  public List<Class> getPossibleTypes() {
    return Arrays.stream(this.annotation.value())
        .map(XmlElementRef::type)
        .filter(c -> !c.equals(XmlElementRef.DEFAULT.class))
        .collect(Collectors.toList());
  }


}
