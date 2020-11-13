package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.generated.JComMessage;
import org.apache.commons.io.IOUtils;
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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static de.djgames.jonas.jcom2.server.StartServer.logger;

public class JComOutputStream {
    private final OutputStream outputStream;
    private final Marshaller marshaller;

    public JComOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(getClass().getResource("/xsd/jComMessage.xsd"));
            JAXBContext jaxbContext = JAXBContext.newInstance(JComMessage.class);
            this.marshaller = jaxbContext.createMarshaller();
            this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            this.marshaller.setSchema(schema);
            this.marshaller.setEventHandler(event -> false);
        } catch (JAXBException | SAXException e) {
            logger.fatal(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void writeString(String text) throws IOException {
        byte[] header = new BigInteger(Integer.toString(text.length())).toByteArray();
        //header does not return 4 bytes
        byte[] headerToSend = new byte[4];
        System.arraycopy(header, 0, headerToSend, 4 - header.length, header.length);

        IOUtils.write(headerToSend, this.outputStream);
        IOUtils.write(text, this.outputStream, StandardCharsets.UTF_8);
    }

    public boolean write(JComMessage jComMessage) throws IOException {
        try {
            this.writeString(jComToXML(jComMessage));
            this.flush();
            return true;
        } catch (JAXBException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    public String jComToXML(JComMessage jComMessage) throws JAXBException {
        StringWriter stringWriter = new StringWriter();
        this.marshaller.marshal(jComMessage, stringWriter);
        return stringWriter.toString();
    }

    public void flush() throws IOException {
        this.outputStream.flush();
    }

    public void close() throws IOException {
        this.outputStream.close();
    }
}
