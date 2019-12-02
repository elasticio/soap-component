package io.elastic.soap.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.elastic.soap.utils.Utils;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;

public abstract class AbstractChoiceDeserializer extends JsonDeserializer {

  protected final Class rawType;
  protected final JavaType javaType;
  protected final ObjectMapper mapper;

  protected AbstractChoiceDeserializer(JavaType javaType) {
    this.javaType = javaType;
    this.rawType = javaType.getRawClass();
    this.mapper = Utils.getConfiguredObjectMapper();
  }

  /**
   * How this works: axios converts wsdl choice element to one of the following structure:
   * 1. field with annotation XmlElements that contains array of XmlElement. XmlElement has property name and property type(java class of choice)
   * 2. field with annotation XmlElementsRefs that contains array of XmlElementReg. XmlElementRef has property name and property type(java class of choice)
   * Field created by axios usually looks like: List<Object> or List<Serializble>. Note in runtime we will have: List
   * This method do the following:
   * 1. Check that json value is possible to convert one of type provided by XmlElement or XmlElementRef annotations.
   * 2. Converts value to type of field created by axios.
   * @param p jackson parser.
   * @param ctxt jackson context.
   * @return deserialize value of choice element.
   */
  @Override
  public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    final JsonNode targetNode = p.getCodec().readTree(p);
    nodeOneOfPossibleTypes(targetNode, this.getPossibleTypes());
    return this.mapper.convertValue(targetNode, this.rawType);
  }

  /**
   * @return List of possible types of choice element.
   */
  public abstract List<Class> getPossibleTypes();

  /**
   * Checks that provided JsonNode from choice element has structure of one of choice element.
   * In case of JAXBElement type was generated it impossible to check the structure, in this case true will be returned.
   * @param node node to be checked
   * @param possibleTypes possible types of node
   * @throws IllegalArgumentException if node structure is not one of provided possible types.
   */
  public void nodeOneOfPossibleTypes(final JsonNode node, final List<Class> possibleTypes) throws IllegalArgumentException {
    if (possibleTypes.contains(JAXBElement.class)) {
      return;
    }
    boolean result = false;
    for (Class type : possibleTypes) {
      result = nodeCanBeConvertedToType(node, type);
      if (result) {
        break;
      }
    }
    if (!result && isNodeAndRawTypeArray(node)) {
      result = handleArrayNode(node, possibleTypes);
    }
    if (!result) {
      throw new IllegalArgumentException(constructExceptionString(node, possibleTypes));
    }
  }

  /**
   * @param node json node
   * @return return true if node and raw type is array
   */
  public boolean isNodeAndRawTypeArray(final JsonNode node) {
    return node.isArray() && (this.javaType.isArrayType() || this.javaType.isCollectionLikeType());
  }
  /**
   * Checks each item of node over provided possible types also each item in array must have same type.
   * @param arrayNode ArrayNode
   * @param possibleTypes possible types of node
   * @return true if each item of array node can be converted to one of possible type.
   */
  public boolean handleArrayNode(JsonNode arrayNode, List<Class> possibleTypes) {
    Class targetType = null;
    for (JsonNode node : arrayNode) {
      targetType = Optional.ofNullable(targetType).orElse(findNodeType(node, possibleTypes));
      boolean canBeConverted = nodeCanBeConvertedToType(node, targetType);
      if (!canBeConverted) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param node json node.
   * @param possibleTypes possible types of node.
   * @return type of node
   * @throws IllegalArgumentException if node can be converted to any of provided possibleTypes
   */
  private Class findNodeType(JsonNode node, List<Class> possibleTypes) {
    for (Class type : possibleTypes) {
      if (nodeCanBeConvertedToType(node, type)) {
        return type;
      }
    }
    throw new IllegalArgumentException(constructExceptionString(node, possibleTypes));
  }


  /**
   *
   * @param node json node.
   * @param type type to be checked.
   * @return true if node can be converted to provided type, false otherwise.
   */
  public boolean nodeCanBeConvertedToType(final JsonNode node, Class type) {
    try {
      this.mapper.convertValue(node, type);
      return true;
    } catch (IllegalArgumentException ex) {
      return false;
    }
  }

  public String constructExceptionString(final JsonNode value, final List<Class> possibleTypes) {
    final StringBuilder bd = new StringBuilder("Failed to convert choice value: ");
    bd.append(value.toString()).append("to one of: ");
    bd.append(possibleTypes.stream().map(Class::getSimpleName).collect(Collectors.joining(","))).append(".");
    return bd.toString();
  }
}
