package net.jcom.jchess.server.factory;


import net.jcom.jchess.server.generated.*;

import java.util.UUID;

public class JChessMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    private JChessMessageFactory() {
    }

    public static JChessMessage createAcceptMessage(UUID playerId, ErrorType errorType) {
        var msg = createBaseMessage(playerId, JChessMessageType.ACCEPT);
        var acceptMsg = new AcceptMessage();
        acceptMsg.setErrorTypeCode(errorType);
        acceptMsg.setAccept(errorType == ErrorType.NO_ERROR);
        msg.setAccept(acceptMsg);
        return msg;
    }

    public static JChessMessage createLoginMessage(UUID playerId, String teamName) {
        var msg = createBaseMessage(playerId, JChessMessageType.LOGIN);
        var logMsg = new LoginMessage();
        logMsg.setName(teamName);
        msg.setLogin(logMsg);
        return msg;
    }

    public static JChessMessage createLoginReplyMessage(UUID newPlayerId) {
        var msg = createBaseMessage(newPlayerId, JChessMessageType.LOGIN_REPLY);
        var logRplyMsg = new LoginReplyMessage();
        logRplyMsg.setNewId(newPlayerId.toString());
        msg.setLoginReply(logRplyMsg);
        return msg;
    }

    public static JChessMessage createHeartbeatMessage(UUID playerId) {
        var msg = createBaseMessage(playerId, JChessMessageType.HEART_BEAT);
        var hbMsg = new HeartBeatMessage();
        msg.setHeartBeat(hbMsg);
        return msg;
    }

    public static JChessMessage createDisconnectMessage(UUID playerId, String name, ErrorType errorType) {
        var msg = createBaseMessage(playerId, JChessMessageType.DISCONNECT);
        var disconnectMsg = new DisconnectMessage();
        disconnectMsg.setErrorTypeCode(errorType);
        msg.setDisconnect(disconnectMsg);
        return msg;
    }

    public static JChessMessage createMatchFoundMessage(UUID playerId, UUID matchId, MatchFormatData matchFormatData,
                                                        String enemyName) {
        var msg = createBaseMessage(playerId, JChessMessageType.MATCH_FOUND);
        var matchFoundMsg = new MatchFoundMessage();
        matchFoundMsg.setMatchId(matchId.toString());
        matchFoundMsg.setMatchFormat(matchFormatData);
        matchFoundMsg.setEnemyName(enemyName);
        msg.setMatchFound(matchFoundMsg);
        return msg;
    }

    public static JChessMessage createMatchOverMessage(UUID playerId, MatchFormatData matchFormatData,
                                                       MatchStatusData matchStatusData) {
        var msg = createBaseMessage(playerId, JChessMessageType.MATCH_OVER);
        var matchOverMsg = new MatchOverMessage();
        matchOverMsg.setMatchFormat(matchFormatData);
        matchOverMsg.setMatchStatus(matchStatusData);
        matchOverMsg.setStatistics("");
        msg.setMatchOver(matchOverMsg);
        return msg;
    }

    public static JChessMessage createMatchStatusMessage(UUID playerId, MatchFormatData matchFormatData,
                                                         MatchStatusData matchStatusData) {
        var msg = createBaseMessage(playerId, JChessMessageType.MATCH_STATUS);
        var matchStatusMsg = new MatchStatusMessage();
        matchStatusMsg.setMatchStatus(matchStatusData);
        msg.setMatchStatus(matchStatusMsg);
        return msg;
    }

    public static JChessMessage createGameStartMessage(UUID playerId, String nameWhite) {
        var msg = createBaseMessage(playerId, JChessMessageType.GAME_START);
        var gameStartMsg = new GameStartMessage();
        gameStartMsg.setNameWhite(nameWhite);
        msg.setGameStart(gameStartMsg);
        return msg;
    }

    public static JChessMessage createGameOverMessage(UUID playerId, boolean isDraw, String winner) {
        var msg = createBaseMessage(playerId, JChessMessageType.GAME_OVER);
        var gameOverMsg = new GameOverMessage();
        gameOverMsg.setIsDraw(isDraw);
        gameOverMsg.setWinner(winner);
        msg.setGameOver(gameOverMsg);
        return msg;
    }

    public static JChessMessage createAwaitMoveMessage(UUID playerId, String position, MoveData lastMove,
                                                       TimeControlData timeControlData) {
        var msg = createBaseMessage(playerId, JChessMessageType.AWAIT_MOVE);
        var awaitMoveMsg = new AwaitMoveMessage();
        awaitMoveMsg.setPosition(position);
        awaitMoveMsg.setLastMove(lastMove);
        awaitMoveMsg.setTimeControl(timeControlData);
        msg.setAwaitMove(awaitMoveMsg);
        return msg;
    }

    public static JChessMessage createMoveMessage(UUID playerId, MoveData move) {
        var msg = createBaseMessage(playerId, JChessMessageType.MOVE);
        var moveMsg = new MoveMessage();
        moveMsg.setMove(move);
        msg.setMove(moveMsg);
        return msg;
    }

    public static JChessMessage createRequestDrawMessage(UUID playerId, RequestDrawType requestDrawType) {
        var msg = createBaseMessage(playerId, JChessMessageType.REQUEST_DRAW);
        var reqDrawMsg = new RequestDrawMessage();
        reqDrawMsg.setReason(requestDrawType);
        msg.setRequestDraw(reqDrawMsg);
        return msg;
    }

    public static JChessMessage createDrawResponseMessage(UUID playerId, boolean accept) {
        var msg = createBaseMessage(playerId, JChessMessageType.DRAW_RESPONSE);
        var drawResMsg = new DrawResponseMessage();
        drawResMsg.setAccept(accept);
        msg.setDrawResponse(drawResMsg);
        return msg;
    }

    private static JChessMessage createBaseMessage(UUID playerId, JChessMessageType type) {
        var msg = objectFactory.createJChessMessage();
        msg.setMessageType(type);
        msg.setPlayerId(playerId.toString());
        return msg;
    }
}
