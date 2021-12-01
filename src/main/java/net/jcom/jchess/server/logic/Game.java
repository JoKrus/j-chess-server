package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.networking.Player;

import java.util.List;

public class Game {
    private List<Player> playerList;

    public Game(List<Player> playerList) {
        this.playerList = playerList;
    }


    public void start() {

    }

    public ChessResult getResult() {
        return ChessResult.DRAW;
    }
}
