//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.3.2 generiert 
// Siehe <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2020.10.25 um 04:50:33 PM CET 
//


package de.djgames.jonas.jcom2.server.generated;

import javax.xml.bind.annotation.*;


/**
 * <p>Java-Klasse für anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="messageType" use="required" type="{}JComMessageType" /&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "JComMessage")
public class JComMessage {

    @XmlAttribute(name = "messageType", required = true)
    protected JComMessageType messageType;
    @XmlAttribute(name = "id", required = true)
    protected int id;

    /**
     * Ruft den Wert der messageType-Eigenschaft ab.
     *
     * @return possible object is
     * {@link JComMessageType }
     */
    public JComMessageType getMessageType() {
        return messageType;
    }

    /**
     * Legt den Wert der messageType-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link JComMessageType }
     */
    public void setMessageType(JComMessageType value) {
        this.messageType = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     */
    public int getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     */
    public void setId(int value) {
        this.id = value;
    }

}
