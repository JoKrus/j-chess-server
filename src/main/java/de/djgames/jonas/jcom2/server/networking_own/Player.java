package de.djgames.jonas.jcom2.server.networking_own;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (!Objects.equals(id, player.id)) return false;
        if (!Objects.equals(communicator, player.communicator))
            return false;
        return Objects.equals(playerName, player.playerName);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (communicator != null ? communicator.hashCode() : 0);
        result = 31 * result + (playerName != null ? playerName.hashCode() : 0);
        return result;
    }
}
