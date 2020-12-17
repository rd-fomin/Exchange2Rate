package com.exchange.model;

import javax.xml.bind.annotation.*;

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
 *         &lt;element name="NumCode" type="{http://www.w3.org/2001/XMLSchema}unsignedShort"/>
 *         &lt;element name="CharCode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Nominal" type="{http://www.w3.org/2001/XMLSchema}unsignedInt"/>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Value" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ID" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "numCode",
        "charCode",
        "nominal",
        "name",
        "value"
})
public class Valute implements Cloneable {

    @XmlElement(name = "NumCode")
    @XmlSchemaType(name = "unsignedShort")
    protected int numCode;
    @XmlElement(name = "CharCode", required = true)
    protected String charCode;
    @XmlElement(name = "Nominal")
    @XmlSchemaType(name = "unsignedInt")
    protected long nominal;
    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "Value", required = true)
    protected String value;
    @XmlAttribute(name = "ID", required = true)
    protected String id;
    @XmlTransient
    private boolean isSelected;

    public Valute() {
        this.isSelected = false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void changeSelect() {
        isSelected = !this.isSelected;
    }

    /**
     * Gets the value of the numCode property.
     *
     */
    public int getNumCode() {
        return numCode;
    }

    /**
     * Sets the value of the numCode property.
     *
     */
    public void setNumCode(int value) {
        this.numCode = value;
    }

    /**
     * Gets the value of the charCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCharCode() {
        return charCode;
    }

    /**
     * Sets the value of the charCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCharCode(String value) {
        this.charCode = value;
    }

    /**
     * Gets the value of the nominal property.
     *
     */
    public long getNominal() {
        return nominal;
    }

    /**
     * Sets the value of the nominal property.
     *
     */
    public void setNominal(long value) {
        this.nominal = value;
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
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the id property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value
     *     allowed object is
     *     {@link String}
     *
     */
    public void setID(String value) {
        this.id = value;
    }

    @Override
    public String toString() {
        return "Valute{" +
                "numCode=" + numCode +
                ", charCode='" + charCode + '\'' +
                ", nominal=" + nominal +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public Valute clone() throws CloneNotSupportedException {
        return (Valute) super.clone();
    }

}