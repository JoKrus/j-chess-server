package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.generated.*;

import java.util.UUID;

public class JComMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    private JComMessageFactory() {
    }

    public static JComMessage createLoginMessage(UUID id, String teamName) {
        var msg = createBaseMessage(id, JComMessageType.LOGIN);
        var logMsg = new LoginMessage();
        logMsg.setName(teamName);
        msg.setLogin(logMsg);
        return msg;
    }

    public static JComMessage createLoginReplyMessage(UUID newId) {
        var msg = createBaseMessage(newId, JComMessageType.LOGIN_REPLY);
        var logRplyMsg = new LoginReplyMessage();
        logRplyMsg.setNewId(newId.toString());
        msg.setLoginReply(logRplyMsg);
        return msg;
    }

    public static JComMessage createAcceptMessage(UUID playerID, ErrorType errorType) {
        var msg = createBaseMessage(playerID, JComMessageType.ACCEPT);
        var acceptMsg = new AcceptMessage();
        acceptMsg.setErrorTypeCode(errorType);
        acceptMsg.setAccept(errorType == ErrorType.NO_ERROR);
        msg.setAccept(acceptMsg);
        return msg;
    }

    private static JComMessage createBaseMessage(UUID id, JComMessageType type) {
        var msg = objectFactory.createJComMessage();
        msg.setMessageType(type);
        msg.setId(id.toString());
        return msg;
    }
}
