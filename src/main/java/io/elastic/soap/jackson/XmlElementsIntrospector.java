package io.elastic.soap.jackson;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.util.*;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElements;

public class XmlElementsIntrospector extends JacksonAnnotationIntrospector {

  /**
   * Finds alias names of field in annotations XmlElements and XmlElementRefs.
   * @param a annotated field.
   * @return alias names of field.
   */
  @Override
  public List<PropertyName> findPropertyAliases(Annotated a) {
    if (a.hasAnnotation(XmlElements.class) || a.hasAnnotation(XmlElementRefs.class)) {
      final List<PropertyName> result = Optional.ofNullable(super.findPropertyAliases(a)).orElse(new ArrayList<>());
      final List<PropertyName> names = getXmlElementsNames(a);
      result.addAll(names);
      return result;
    }
    return super.findPropertyAliases(a);
  }

  /**
   * Return custom deserializer in case field annotated with XmlElements or XmlElementRefs annotations.
   * @param a annotated field.
   * @return deserializer for field.
   */
  @Override
  public Object findDeserializer(Annotated a) {
    if (a.hasAnnotation(XmlElementRefs.class)) {
      return new XmlElementRefsChoiceDeserializer(a.getType(), a.getAnnotation(XmlElementRefs.class));
    }
    if (a.hasAnnotation(XmlElements.class)) {
      return new XmlElementsChoiceDeserializer(a.getType(), a.getAnnotation(XmlElements.class));
    }
    return super.findDeserializer(a);
  }

  @Override
  public Object findSerializationConverter(Annotated a) {
    if ((a.hasAnnotation(XmlElementRefs.class) || a.hasAnnotation(XmlElements.class)) && a.getType().isCollectionLikeType()) {
      return new JaxbElementsJsonValueConverter();
    }
    return super.findSerializationConverter(a);
  }

  public List<PropertyName> getXmlElementsNames(final Annotated a) {
    if (a.hasAnnotation(XmlElements.class)) {
      return Arrays.stream(a.getAnnotation(XmlElements.class).value()).map(e -> new PropertyName(e.name())).collect(Collectors.toList());
    }
    return Arrays.stream(a.getAnnotation(XmlElementRefs.class).value()).map(e -> new PropertyName(e.name())).collect(Collectors.toList());
  }
}
