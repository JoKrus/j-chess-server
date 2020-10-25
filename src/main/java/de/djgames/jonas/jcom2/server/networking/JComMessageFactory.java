package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.generated.JComMessageType;
import de.djgames.jonas.jcom2.server.generated.ObjectFactory;

public class JComMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    private JComMessageFactory() {
    }

    public static JComMessage createTestMessage() {
        var msg = objectFactory.createJComMessage();
        msg.setMessageType(JComMessageType.LOGIN);
        return msg;
    }
}
