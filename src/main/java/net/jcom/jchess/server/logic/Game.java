package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.factory.JChessMessageFactory;
import net.jcom.jchess.server.generated.MoveData;
import net.jcom.jchess.server.networking.Player;

import java.util.HashMap;
import java.util.List;

public class Game {
    private List<Player> playerList;
    HashMap<Color, Player> colorPlayerMap;
    MoveData lastMove = null;
    ChessResult result;
    private Position position;
    //TimeControl

    public Game(List<Player> playerList) {
        this.playerList = playerList;
        this.colorPlayerMap = new HashMap<>();
        this.colorPlayerMap.put(Color.WHITE, playerList.get(0));
        this.colorPlayerMap.put(Color.BLACK, playerList.get(1));
        this.result = ChessResult.PLAYING;
    }


    public void start() {
        List<Player> list = this.playerList;
        for (int i = 0; i < list.size(); i++) {
            Player player = list.get(i);
            player.getCommunicator().sendMessage(JChessMessageFactory.createGameStartMessage(player.getId(),
                    this.colorPlayerMap.get(Color.WHITE).getPlayerName()));
        }

        this.position = new Position();

        gameLoop:
        while (isOver() == ChessResult.PLAYING) {
            Player currentPlayer = this.colorPlayerMap.get(this.position.getCurrent());
            currentPlayer.getCommunicator().sendMessage(JChessMessageFactory.createAwaitMoveMessage(currentPlayer.getId(), this.position.toFenNotation(), this.lastMove));
            var message = currentPlayer.getCommunicator().receiveMessage();

            switch (message.getMessageType()) {
                case MOVE:
                    break;
                case REQUEST_DRAW:
                    break;
                default:
                    this.result = this.position.getCurrent().enemyResult();
                    break gameLoop;
            }
        }
    }

    //gets called with current = player to make his move
    public ChessResult isOver() {
        //wins
        //resign? not planned to be implemented
        //timeout

        //draws

        // 50 move rule
        // draw offer after move 40

        //Position checks
        ChessResult overBasedOnPosition = this.position.isOverBasedOnPosition();
        if (overBasedOnPosition != ChessResult.PLAYING) {
            return overBasedOnPosition;
        }
        return ChessResult.PLAYING;
    }

    public ChessResult getResult() {
        return ChessResult.DRAW;
    }
}
