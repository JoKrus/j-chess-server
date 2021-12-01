package net.jcom.jchess.server.networking;

import java.util.Objects;
import java.util.UUID;

public class Player {
    private final UUID id;
    private final Communicator communicator;
    private final String playerName;
    private PlayerStatus status;
    //TODO role (Player / Spectator)

    public Player(UUID id, Communicator communicator, String playerName) {
        this.id = id;
        this.communicator = communicator;
        this.playerName = playerName;
        this.status = PlayerStatus.QUEUE;
    }

    public UUID getId() {
        return this.id;
    }

    public Communicator getCommunicator() {
        return this.communicator;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public PlayerStatus getStatus() {
        return this.status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (!Objects.equals(this.id, player.id)) return false;
        if (!Objects.equals(this.communicator, player.communicator))
            return false;
        return Objects.equals(this.playerName, player.playerName);
    }

    @Override
    public int hashCode() {
        int result = this.id != null ? this.id.hashCode() : 0;
        result = 31 * result + (this.communicator != null ? this.communicator.hashCode() : 0);
        result = 31 * result + (this.playerName != null ? this.playerName.hashCode() : 0);
        return result;
    }
}
