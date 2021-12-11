package net.jcom.jchess.server.factory;

import net.jcom.jchess.server.generated.*;

import java.util.UUID;

public class JChessMessageFactory {
    private static final ObjectFactory objectFactory = new ObjectFactory();

    private JChessMessageFactory() {
    }

    public static JChessMessage createAcceptMessage(UUID playerId, ErrorType errorType) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.ACCEPT);
        AcceptMessage acceptMsg = new AcceptMessage();
        acceptMsg.setErrorTypeCode(errorType);
        acceptMsg.setAccept(errorType == ErrorType.NO_ERROR);
        msg.setAccept(acceptMsg);
        return msg;
    }

    public static JChessMessage createLoginMessage(UUID playerId, String teamName) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.LOGIN);
        LoginMessage logMsg = new LoginMessage();
        logMsg.setName(teamName);
        msg.setLogin(logMsg);
        return msg;
    }

    public static JChessMessage createLoginReplyMessage(UUID newPlayerId) {
        JChessMessage msg = createBaseMessage(newPlayerId, JChessMessageType.LOGIN_REPLY);
        LoginReplyMessage logRplyMsg = new LoginReplyMessage();
        logRplyMsg.setNewId(newPlayerId.toString());
        msg.setLoginReply(logRplyMsg);
        return msg;
    }

    public static JChessMessage createHeartbeatMessage(UUID playerId) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.HEART_BEAT);
        HeartBeatMessage hbMsg = new HeartBeatMessage();
        msg.setHeartBeat(hbMsg);
        return msg;
    }

    public static JChessMessage createDisconnectMessage(UUID playerId, ErrorType errorType) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.DISCONNECT);
        DisconnectMessage disconnectMsg = new DisconnectMessage();
        disconnectMsg.setErrorTypeCode(errorType);
        msg.setDisconnect(disconnectMsg);
        return msg;
    }

    public static JChessMessage createMatchFoundMessage(UUID playerId, UUID matchId, MatchFormatData matchFormatData, String enemyName) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.MATCH_FOUND);
        MatchFoundMessage matchFoundMsg = new MatchFoundMessage();
        matchFoundMsg.setMatchId(matchId.toString());
        matchFoundMsg.setMatchFormat(matchFormatData);
        matchFoundMsg.setEnemyName(enemyName);
        msg.setMatchFound(matchFoundMsg);
        return msg;
    }

    public static JChessMessage createMatchOverMessage(UUID playerId, MatchFormatData matchFormatData, MatchStatusData matchStatusData) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.MATCH_OVER);
        MatchOverMessage matchOverMsg = new MatchOverMessage();
        matchOverMsg.setMatchFormat(matchFormatData);
        matchOverMsg.setMatchStatus(matchStatusData);
        matchOverMsg.setStatistics("");
        msg.setMatchOver(matchOverMsg);
        return msg;
    }

    public static JChessMessage createMatchStatusMessage(UUID playerId, MatchStatusData matchStatusData) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.MATCH_STATUS);
        MatchStatusMessage matchStatusMsg = new MatchStatusMessage();
        matchStatusMsg.setMatchStatus(matchStatusData);
        msg.setMatchStatus(matchStatusMsg);
        return msg;
    }

    public static JChessMessage createGameStartMessage(UUID playerId, String nameWhite) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.GAME_START);
        GameStartMessage gameStartMsg = new GameStartMessage();
        gameStartMsg.setNameWhite(nameWhite);
        msg.setGameStart(gameStartMsg);
        return msg;
    }

    public static JChessMessage createGameOverMessage(UUID playerId, boolean isDraw, String winner, String pgn) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.GAME_OVER);
        GameOverMessage gameOverMsg = new GameOverMessage();
        gameOverMsg.setIsDraw(isDraw);
        gameOverMsg.setWinner(winner);
        gameOverMsg.setPgn(pgn);
        msg.setGameOver(gameOverMsg);
        return msg;
    }

    public static JChessMessage createAwaitMoveMessage(UUID playerId, String position, MoveData lastMove, TimeControlData timeControlData) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.AWAIT_MOVE);
        AwaitMoveMessage awaitMoveMsg = new AwaitMoveMessage();
        awaitMoveMsg.setPosition(position);
        awaitMoveMsg.setLastMove(lastMove);
        awaitMoveMsg.setTimeControl(timeControlData);
        msg.setAwaitMove(awaitMoveMsg);
        return msg;
    }

    public static JChessMessage createMoveMessage(UUID playerId, MoveData move) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.MOVE);
        MoveMessage moveMsg = new MoveMessage();
        moveMsg.setMove(move);
        msg.setMove(moveMsg);
        return msg;
    }

    public static JChessMessage createRequestDrawMessage(UUID playerId, RequestDrawType requestDrawType) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.REQUEST_DRAW);
        RequestDrawMessage reqDrawMsg = new RequestDrawMessage();
        reqDrawMsg.setReason(requestDrawType);
        msg.setRequestDraw(reqDrawMsg);
        return msg;
    }

    public static JChessMessage createDrawResponseMessage(UUID playerId, boolean accept) {
        JChessMessage msg = createBaseMessage(playerId, JChessMessageType.DRAW_RESPONSE);
        DrawResponseMessage drawResMsg = new DrawResponseMessage();
        drawResMsg.setAccept(accept);
        msg.setDrawResponse(drawResMsg);
        return msg;
    }

    private static JChessMessage createBaseMessage(UUID playerId, JChessMessageType type) {
        JChessMessage msg = objectFactory.createJChessMessage();
        msg.setMessageType(type);
        msg.setPlayerId(playerId.toString());
        return msg;
    }
}
