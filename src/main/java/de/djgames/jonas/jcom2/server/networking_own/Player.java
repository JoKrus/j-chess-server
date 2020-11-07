package de.djgames.jonas.jcom2.server.networking_own;

import java.util.UUID;

public class Player {
    private final UUID id;
    private final Communicator communicator;
    private final String playerName;
    //TODO role

    public Player(UUID id, Communicator communicator, String playerName) {
        this.id = id;
        this.communicator = communicator;
        this.playerName = playerName;
    }

    public UUID getId() {
        return id;
    }

    public Communicator getCommunicator() {
        return communicator;
    }

    public String getPlayerName() {
        return playerName;
    }


}
