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
import java.io.*;

public class XmlInputStream extends UTFInputStream {

    private Unmarshaller unmarshaller;

    public XmlInputStream(InputStream inputStream) {
        super(inputStream);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JComMessage.class);
            unmarshaller = jaxbContext.createUnmarshaller();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                // muss getResourceAsStream() statt getResource() sein
                // damit es auch in jars funktioniert
                InputStream resourceAsStream = getClass().getResourceAsStream("/xsd/jComMessage.xsd");
                // Der Inputstream resourceAsStream wird in die Datei temp.xsd
                // geschrieben und dann dem Schema uebergeben
                // XXX: Kein bessere Implementierung gefunden
                File tempFile = File.createTempFile("temp", ".xsd");
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                int read;
                byte[] bytes = new byte[1024];
                while ((read = resourceAsStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, read);
                }
                fileOutputStream.close();
                Schema schema = schemaFactory.newSchema(tempFile);
                unmarshaller.setSchema(schema);
                unmarshaller.setEventHandler(validationEvent -> {
                    System.out.println("ich bin ein eventhandler");
                    return false;
                });
                tempFile.deleteOnExit();
            } catch (SAXException e) {
                e.printStackTrace();
                Logger.fatal("aah");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JAXBException e) {
            Logger.fatal("aahbee");
        }
    }

    public XmlInputStream() {
        this(System.in);
    }

    /**
     * Liest eine Nachricht und gibt die entsprechende Instanz zurueck
     *
     * @return JcomMessage
     * @throws IOException e
     */
    public JComMessage readJCom() throws IOException, UnmarshalException {
        JComMessage result = null;
        try {
            String xml = this.readUTF8();
            result = XMLToJCom(xml);
            Logger.debug("XmlInputStream.received");
            Logger.debug(xml);
        } catch (UnmarshalException e) {
            throw e;
        } catch (JAXBException e) {
            e.printStackTrace();
            Logger.error("XmlInputStream.errorUnmarshalling");
        } catch (NullPointerException e) {
            Logger.error("XmlInputStream.nullpointerWhileReading");
        }
        return result;
    }

    public JComMessage XMLToJCom(String xml) throws JAXBException {
        StringReader stringReader = new StringReader(xml);
        return (JComMessage) this.unmarshaller.unmarshal(stringReader);
    }

}