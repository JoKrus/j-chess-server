package de.djgames.jonas.jcom2.server.sandbox;

import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.networking.JComMessageFactory;
import de.djgames.jonas.jcom2.server.networking.XmlInputStream;
import de.djgames.jonas.jcom2.server.networking.XmlOutputStream;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.UUID;

public class SandboxMain {
    public static void main(String[] args) throws IOException, JAXBException {
        JComMessage jComMessage = new XmlInputStream().XMLToJCom(
                new XmlOutputStream(System.out).jComToXML(JComMessageFactory.createLoginReplyMessage(UUID.randomUUID())));
        System.out.println(jComMessage.getMessageType());
    }
}
