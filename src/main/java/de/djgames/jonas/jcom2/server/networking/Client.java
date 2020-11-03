package de.djgames.jonas.jcom2.server.networking;

import java.util.Objects;
import java.util.UUID;

public class Client {
    private final UUID id;
    private final String name;
    private final Connection connection;

    public Client(UUID id, String name, Connection connection) {
        this.id = id;
        this.name = name;
        this.connection = connection;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return getId() == client.getId() &&
                Objects.equals(getConnection(), client.getConnection());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getConnection());
    }
}
