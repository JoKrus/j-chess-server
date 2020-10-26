package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.logging.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

public class XmlOutputStream extends UTFOutputStream {

    private Marshaller marshaller;

    public XmlOutputStream(OutputStream outputStream) {
        super(outputStream);
        // Anlegen der JAXB-Komponenten
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(getClass().getResource("/xsd/jcomMessage.xsd"));

            JAXBContext jaxbContext = JAXBContext.newInstance(JComMessage.class);
            this.marshaller = jaxbContext.createMarshaller();
            this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setSchema(schema);
            marshaller.setEventHandler(event -> true);
        } catch (JAXBException jaxbException) {
            Logger.debug("XmlOutputStream.ErrorInitialisingJAXBComponent");
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Versenden einer XML Nachricht
     *
     * @param jComMessage Message to write
     */
    public void write(JComMessage jComMessage) throws IOException {
        // Generierung des fertigen XML
        try {
            // Versenden des XML
            this.writeUTF8(jComToXML(jComMessage));
            this.flush();
        } catch (JAXBException e) {
            Logger.info("XmlOutputStream.errorSendingMessage", e);
        }
    }

    /**
     * Stellt ein JCom-Objekt als XML dar
     *
     * @param jComMessage darzustellendes JCom-Objekt
     * @return XML-Darstellung als String
     * @throws JAXBException e
     */
    public String jComToXML(JComMessage jComMessage) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        this.marshaller.marshal(jComMessage, stringWriter);
        return stringWriter.toString();
    }

}
