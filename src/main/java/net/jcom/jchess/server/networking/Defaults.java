package net.jcom.jchess.server.networking;

import java.util.UUID;

public class Defaults {
    public static final UUID DEFAULT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final Player DEFAULT_PLAYER = new Player(DEFAULT_UUID, null, "");

}
