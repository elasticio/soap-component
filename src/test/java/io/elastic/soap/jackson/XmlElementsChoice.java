
package io.elastic.soap.jackson;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for XmlElementsChoice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="XmlElementsChoice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element name="IntField" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="StringField" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ComplexType" type="{http://elatic.io/test}ComplexType" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XmlElementsChoice", propOrder = {
    "intFieldOrStringFieldOrComplexType"
})
public class XmlElementsChoice {

    @XmlElements({
        @XmlElement(name = "IntField", type = Integer.class),
        @XmlElement(name = "StringField", type = String.class),
        @XmlElement(name = "ComplexType", type = ComplexType.class)
    })
    protected List<Object> intFieldOrStringFieldOrComplexType;

    /**
     * Gets the value of the intFieldOrStringFieldOrComplexType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the intFieldOrStringFieldOrComplexType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIntFieldOrStringFieldOrComplexType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * {@link String }
     * {@link ComplexType }
     * 
     * 
     */
    public List<Object> getIntFieldOrStringFieldOrComplexType() {
        if (intFieldOrStringFieldOrComplexType == null) {
            intFieldOrStringFieldOrComplexType = new ArrayList<Object>();
        }
        return this.intFieldOrStringFieldOrComplexType;
    }

}
