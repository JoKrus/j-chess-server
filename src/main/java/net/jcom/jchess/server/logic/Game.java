package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.factory.JChessMessageFactory;
import net.jcom.jchess.server.generated.JChessMessage;
import net.jcom.jchess.server.generated.MoveData;
import net.jcom.jchess.server.generated.RequestDrawType;
import net.jcom.jchess.server.generated.TimeControlData;
import net.jcom.jchess.server.networking.Defaults;
import net.jcom.jchess.server.networking.Player;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class Game {
    private final List<Player> playerList;
    private final HashMap<Color, Player> colorPlayerMap;
    private final HashMap<Color, Long> timeLeft;
    private ChessResult result;
    private Position position;
    private final Scheduler scheduler;
    private final int round;
    private MoveData lastMove = null;

    public Game(List<Player> playerList, int round) {
        this.playerList = playerList;
        this.round = round;
        this.colorPlayerMap = new HashMap<>();
        this.colorPlayerMap.put(Color.WHITE, playerList.get(0));
        this.colorPlayerMap.put(Color.BLACK, playerList.get(1));
        this.result = ChessResult.PLAYING;
        this.timeLeft = new HashMap<>();
        this.timeLeft.put(Color.WHITE, MatchDefaults.MATCH_FORMAT_DATA.getTimePerSide());
        this.timeLeft.put(Color.BLACK, MatchDefaults.MATCH_FORMAT_DATA.getTimePerSide());
        this.scheduler = new Scheduler();
    }

    public void start() {
        List<Player> list = this.playerList;
        for (int i = 0; i < list.size(); ++i) {
            Player player = list.get(i);
            player.getCommunicator().sendMessage(JChessMessageFactory.createGameStartMessage(player.getId(),
                    this.colorPlayerMap.get(Color.WHITE).getPlayerName()));
        }

        this.position = new Position();

        gameLoop:
        while ((this.result = this.isOver()) == ChessResult.PLAYING) {
            Color currentPlayerColor = this.position.getCurrent();
            Player player = this.colorPlayerMap.get(currentPlayerColor);
            JChessMessage awaitMoveMsg = JChessMessageFactory.createAwaitMoveMessage(player.getId(), this.position.toFenNotation(), this.lastMove, this.toTimeControlData(currentPlayerColor));
            player.getCommunicator().sendMessage(awaitMoveMsg);

            this.scheduler.startTimer(currentPlayerColor, () -> this.result = currentPlayerColor.enemyResult());
            long start = System.currentTimeMillis();
            JChessMessage message = player.getCommunicator().receiveMessage();
            long endMaybe = System.currentTimeMillis();
            this.scheduler.stopTimer(currentPlayerColor);

            if (this.timeLeft.get(currentPlayerColor) - (endMaybe - start) < 0L) {
                this.result = currentPlayerColor.enemyResult();
                break;
            }

            if (message == null) {
                this.result = currentPlayerColor.enemyResult();
                break;
            }

            switch (message.getMessageType()) {
                case MOVE:
                    try {
                        if (this.position.checkIfLegalMove(message.getMove().getMove())) {
                            this.position.playMove(message.getMove().getMove());
                            this.lastMove = message.getMove().getMove();
                            this.timeLeft.put(currentPlayerColor, this.timeLeft.get(currentPlayerColor) - (endMaybe - start));
                            this.timeLeft.put(currentPlayerColor, this.timeLeft.get(currentPlayerColor) + MatchDefaults.MATCH_FORMAT_DATA.getTimePerSideIncrement());
                        } else {
                            this.result = currentPlayerColor.enemyResult();
                            break gameLoop;
                        }
                    } catch (NullPointerException var11) {
                        this.result = currentPlayerColor.enemyResult();
                        break gameLoop;
                    }
                    break;
                case REQUEST_DRAW:
                    if (message.getRequestDraw().getReason().equals(RequestDrawType.FIFTY_MOVE_RULE)) {
                        if (this.position.checkRequestedMoveRule() == ChessResult.DRAW) {
                            this.result = ChessResult.DRAW;
                            break gameLoop;
                        }
                    }
                    this.result = currentPlayerColor.enemyResult();
                    break gameLoop;
                default:
                    this.result = currentPlayerColor.enemyResult();
                    break gameLoop;
            }
        }

        for (int i = 0; i < list.size(); ++i) {
            Player player = list.get(i);
            player.getCommunicator().sendMessage(JChessMessageFactory.createGameOverMessage(player.getId(), this.result == ChessResult.DRAW,
                    this.colorPlayerMap.getOrDefault(this.result.toColor(), Defaults.DEFAULT_PLAYER).getPlayerName(),
                    this.toPgnNotation()));
        }
    }

    public ChessResult isOver() {
        if (this.result != ChessResult.PLAYING) {
            return this.result;
        }
        //TODO resign? not planned to be implemented

        //Position checks
        ChessResult overBasedOnPosition = this.position.isOverBasedOnPosition();
        if (overBasedOnPosition != ChessResult.PLAYING) {
            return overBasedOnPosition;
        }
        return ChessResult.PLAYING;
    }

    public ChessResult getResult() {
        return this.result;
    }

    public TimeControlData toTimeControlData(Color myTeam) {
        TimeControlData timeControlData = new TimeControlData();
        timeControlData.setYourTimeInMs(this.timeLeft.get(myTeam));
        timeControlData.setEnemyTimeInMs(this.timeLeft.get(myTeam.enemy()));
        return timeControlData;
    }


    public String toPgnNotation() {
        StringBuilder sb = new StringBuilder();
        sb.append(tagPairToString("Event", "j-chess online game")).append("\n");
        sb.append(tagPairToString("Site", "chess.j-com.net")).append("\n");
        sb.append(tagPairToString("Date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .replace("-", ".")).append("\n");
        sb.append(tagPairToString("Round", String.format("%d", this.round))).append("\n");
        sb.append(tagPairToString("White", this.colorPlayerMap.get(Color.WHITE).getPlayerName())).append("\n");
        sb.append(tagPairToString("Black", this.colorPlayerMap.get(Color.BLACK).getPlayerName())).append("\n");
        sb.append(tagPairToString("Result", this.getResult().toPgnResult())).append("\n");
        sb.append("\n");

        var movesExtended = this.position.getAllMoves();

        boolean isWhiteMove = true;
        int gameTurn = 0;
        for (int i = 0; i < movesExtended.size(); i++) {
            if (isWhiteMove) {
                gameTurn++;
                sb.append(gameTurn).append(". ");
            }
            sb.append(movesExtended.get(i).toMoveText()).append(" ");

            isWhiteMove = !isWhiteMove;
        }

        sb.append(this.result.toPgnResult()).append("\n\n");

        return sb.toString();
    }

    private String tagPairToString(String tag, String value) {
        return String.format("[%s \"%s\"]", tag, value);
    }
}
