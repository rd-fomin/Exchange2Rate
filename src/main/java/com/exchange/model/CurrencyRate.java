package com.exchange.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Valute" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="NumCode" type="{http://www.w3.org/2001/XMLSchema}unsignedShort"/>
 *                   &lt;element name="CharCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="Nominal" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/>
 *                   &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="Date" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "currencies"
})
@XmlRootElement(name = "ValCurs")
public class CurrencyRate implements Cloneable {

    @XmlElement(name = "Valute", required = true)
    protected List<Currency> currencies;
    @XmlAttribute(name = "Date", required = true)
    protected String date;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the valute property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valute property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValute().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Currency }
     * 
     * 
     */
    public List<Currency> getCurrencies() {
        if (currencies == null) {
            currencies = new ArrayList<>();
        }
        return this.currencies;
    }

    /**
     * Gets the value of the date property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the value of the date property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDate(String value) {
        this.date = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String}
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    @Override
    public String toString() {
        return "CurrencyCurs{" +
                "currencies=" + currencies +
                ", date='" + date + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public CurrencyRate clone() throws CloneNotSupportedException {
        return (CurrencyRate) super.clone();
    }

}
