
package io.elastic.soap.jackson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for XmlElementRefsChoice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XmlElementRefsChoice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="IntField" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="StringField1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="StringField2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="StringField3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="StringField4" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XmlElementRefsChoice", propOrder = {
    "intFieldOrStringField1OrStringField2"
})
public class XmlElementRefsChoice {

    @XmlElementRefs({
        @XmlElementRef(name = "StringField1", namespace = "http://elatic.io/test", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "StringField3", namespace = "http://elatic.io/test", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "StringField4", namespace = "http://elatic.io/test", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "IntField", namespace = "http://elatic.io/test", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "StringField2", namespace = "http://elatic.io/test", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "ComplexType", namespace = "http://elatic.io/test", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<? extends Serializable>> intFieldOrStringField1OrStringField2;

    /**
     * Gets the value of the intFieldOrStringField1OrStringField2 property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the intFieldOrStringField1OrStringField2 property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIntFieldOrStringField1OrStringField2().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link Integer }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends Serializable>> getIntFieldOrStringField1OrStringField2() {
        if (intFieldOrStringField1OrStringField2 == null) {
            intFieldOrStringField1OrStringField2 = new ArrayList<JAXBElement<? extends Serializable>>();
        }
        return this.intFieldOrStringField1OrStringField2;
    }

}
