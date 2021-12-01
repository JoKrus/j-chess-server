package net.jcom.jchess.server.iostreams;

import net.jcom.jchess.server.generated.JChessMessage;
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

public class JChessOutputStream {
    private final OutputStream outputStream;
    private final Marshaller marshaller;

    private final Object sendLock = new Object();

    public JChessOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(getClass().getResource("/xsd/JChessXsd/jComMessage.xsd"));
            JAXBContext jaxbContext = JAXBContext.newInstance(JChessMessage.class);
            this.marshaller = jaxbContext.createMarshaller();
            this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            this.marshaller.setSchema(schema);
            this.marshaller.setEventHandler(event -> false);
        } catch (JAXBException | SAXException e) {
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

    public boolean write(JChessMessage jComMessage) throws IOException {
        synchronized (this.sendLock) {
            try {
                this.writeString(jComToXML(jComMessage));
                this.flush();
                return true;
            } catch (JAXBException e) {
                return false;
            }
        }
    }

    public String jComToXML(JChessMessage jComMessage) throws JAXBException {
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