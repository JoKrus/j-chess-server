package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.generated.*;

import java.util.UUID;

public class JComMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    private JComMessageFactory() {
    }

    public static JComMessage createLoginMessage(UUID playerId, String teamName) {
        var msg = createBaseMessage(playerId, JComMessageType.LOGIN);
        var logMsg = new LoginMessage();
        logMsg.setName(teamName);
        msg.setLogin(logMsg);
        return msg;
    }

    public static JComMessage createLoginReplyMessage(UUID newPlayerId) {
        var msg = createBaseMessage(newPlayerId, JComMessageType.LOGIN_REPLY);
        var logRplyMsg = new LoginReplyMessage();
        logRplyMsg.setNewId(newPlayerId.toString());
        msg.setLoginReply(logRplyMsg);
        return msg;
    }

    public static JComMessage createHeartbeatMessage(UUID playerId) {
        var msg = createBaseMessage(playerId, JComMessageType.HEART_BEAT);
        var hbMsg = new HeartBeatMessage();
        msg.setHeartBeat(hbMsg);
        return msg;
    }

    public static JComMessage createAcceptMessage(UUID playerId, ErrorType errorType) {
        var msg = createBaseMessage(playerId, JComMessageType.ACCEPT);
        var acceptMsg = new AcceptMessage();
        acceptMsg.setErrorTypeCode(errorType);
        acceptMsg.setAccept(errorType == ErrorType.NO_ERROR);
        msg.setAccept(acceptMsg);
        return msg;
    }

    public static JComMessage createDisconnectMessage(UUID playerId, String name, ErrorType errorType) {
        var msg = createBaseMessage(playerId, JComMessageType.DISCONNECT);
        var disconnectMsg = new DisconnectMessage();
        disconnectMsg.setErrorTypeCode(errorType);
        disconnectMsg.setName(name);
        msg.setDisconnect(disconnectMsg);
        return msg;
    }

    public static JComMessage createGameFoundMessage(UUID playerId, UUID gameId, String enemyName) {
        var msg = createBaseMessage(playerId, JComMessageType.GAME_FOUND);
        var gameFoundMsg = new GameFoundMessage();
        gameFoundMsg.setGameId(gameId.toString());
        gameFoundMsg.setEnemyName(enemyName);
        msg.setGameFound(gameFoundMsg);
        return msg;
    }

    private static JComMessage createBaseMessage(UUID playerId, JComMessageType type) {
        var msg = objectFactory.createJComMessage();
        msg.setMessageType(type);
        msg.setPlayerId(playerId.toString());
        return msg;
    }
}
