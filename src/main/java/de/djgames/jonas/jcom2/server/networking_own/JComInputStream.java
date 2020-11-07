package de.djgames.jonas.jcom2.server.networking_own;

import de.djgames.jonas.jcom2.server.generated.JComMessage;
import org.apache.commons.io.IOUtils;
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
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static de.djgames.jonas.jcom2.server.StartServer.logger;

public class JComInputStream {
    private final InputStream inputStream;
    private final Unmarshaller unmarshaller;

    public JComInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
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
                logger.fatal(e.getLocalizedMessage(), e);
                throw new RuntimeException(e);
            }
        } catch (JAXBException e) {
            logger.fatal(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String readMessage() throws IOException {
        int messageLength = readHeader();
        byte[] message = new byte[messageLength];
        IOUtils.read(inputStream, message);
        return new String(message, StandardCharsets.UTF_8);
    }

    private int readHeader() throws IOException {
        byte[] textLength = new byte[4];
        IOUtils.read(inputStream, textLength);
        return new BigInteger(textLength).intValue();
    }

    public JComMessage readJCom() throws IOException, UnmarshalException {
        JComMessage result = null;
        try {
            String xml = this.readMessage();
            result = XMLToJCom(xml);
        } catch (UnmarshalException e) {
            throw e;
        } catch (JAXBException | NullPointerException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return result;
    }

    public JComMessage XMLToJCom(String xml) throws JAXBException {
        StringReader stringReader = new StringReader(xml);
        return (JComMessage) this.unmarshaller.unmarshal(stringReader);
    }

    public void close() throws IOException {
        this.inputStream.close();
    }
}
