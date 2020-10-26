package de.djgames.jonas.jcom2.server.networking;

import java.util.Objects;
import java.util.UUID;

public class Client {
    final UUID id;
    private final String name;
    private final Connection connectionToClient;

    public Client(UUID id, String name, Connection connection) {
        this.id = id;
        this.name = name;
        this.connectionToClient = connection;
    }

    public void run() {

    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Connection getConnectionToClient() {
        return connectionToClient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return getId() == client.getId() &&
                Objects.equals(getConnectionToClient(), client.getConnectionToClient());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getConnectionToClient());
    }
}
