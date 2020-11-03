package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class XmlInputStream extends StringInputStream {

    private Unmarshaller unmarshaller;

    public XmlInputStream(InputStream inputStream) {
        super(inputStream);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JComMessage.class);
            unmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                InputStream resourceAsStream = getClass().getResourceAsStream("/xsd/jComMessage.xsd");
                byte[] xsdFile = resourceAsStream.readAllBytes();
                File tempFile = File.createTempFile("temp", ".xsd");
                FileUtils.writeByteArrayToFile(tempFile, xsdFile);
                Schema schema = schemaFactory.newSchema(tempFile);
                unmarshaller.setSchema(schema);
                unmarshaller.setEventHandler(validationEvent -> false);
                tempFile.deleteOnExit();
            } catch (SAXException e) {
                Logger.fatal(e.getLocalizedMessage(), e);
            } catch (IOException e) {
                Logger.error(e.getLocalizedMessage(), e);
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
            Logger.debug("XML Input - Message receiving");
            String xml = this.readMessage();
            result = XMLToJCom(xml);
            Logger.debug("XML Input - Message received");
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