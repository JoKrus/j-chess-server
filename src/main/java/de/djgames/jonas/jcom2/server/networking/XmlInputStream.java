package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.logging.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

public class XmlInputStream extends StringInputStream {

    private Unmarshaller unmarshaller;

    public XmlInputStream(InputStream inputStream) {
        super(inputStream);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JComMessage.class);
            unmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                URL urlToXsd = getClass().getResource("/xsd/jComMessage.xsd");
                Schema schema = schemaFactory.newSchema(urlToXsd);
                unmarshaller.setSchema(schema);
                unmarshaller.setEventHandler(validationEvent -> false);
            } catch (SAXException e) {
                Logger.fatal(e.getLocalizedMessage(), e);
            }
        } catch (JAXBException e) {
            Logger.fatal(e.getLocalizedMessage(), e);
        }
    }

    public XmlInputStream() {
        this(System.in);
    }

    public JComMessage readJCom() throws IOException, UnmarshalException {
        JComMessage result = null;
        try {
            String xml = this.readMessage();
            result = XMLToJCom(xml);
        } catch (UnmarshalException e) {
            throw e;
        } catch (JAXBException | NullPointerException e) {
            Logger.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    public JComMessage XMLToJCom(String xml) throws JAXBException {
        StringReader stringReader = new StringReader(xml);
        return (JComMessage) this.unmarshaller.unmarshal(stringReader);
    }
}