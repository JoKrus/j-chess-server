package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.factory.JChessMessageFactory;
import net.jcom.jchess.server.generated.JChessMessage;
import net.jcom.jchess.server.generated.MoveData;
import net.jcom.jchess.server.generated.RequestDrawType;
import net.jcom.jchess.server.generated.TimeControlData;
import net.jcom.jchess.server.networking.Defaults;
import net.jcom.jchess.server.networking.Player;

import java.util.HashMap;
import java.util.List;

public class Game {
    private final List<Player> playerList;
    private final HashMap<Color, Player> colorPlayerMap;
    private final HashMap<Color, Long> timeLeft;
    private ChessResult result;
    private Position position;
    private final Scheduler scheduler;
    private MoveData lastMove = null;

    public Game(List<Player> playerList) {
        this.playerList = playerList;
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
                    break;
                default:
                    this.result = currentPlayerColor.enemyResult();
                    break gameLoop;
            }
        }

        for (int i = 0; i < list.size(); ++i) {
            Player player = list.get(i);
            player.getCommunicator().sendMessage(JChessMessageFactory.createGameOverMessage(player.getId(), this.result == ChessResult.DRAW, this.colorPlayerMap.getOrDefault(this.result.toColor(), Defaults.DEFAULT_PLAYER).getPlayerName()));
        }
    }

    public ChessResult isOver() {
        if (this.result != ChessResult.PLAYING) {
            return this.result;
        }
        //TODO resign? not planned to be implemented

        //draws

        // TODO 50 move rule
        // TODO draw offer after move 40

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
}
