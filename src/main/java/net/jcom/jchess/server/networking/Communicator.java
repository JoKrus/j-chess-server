package net.jcom.jchess.server.networking;

import net.jcom.jchess.server.Server;
import net.jcom.jchess.server.exception.InvalidSchemaVersion;
import net.jcom.jchess.server.factory.JChessMessageFactory;
import net.jcom.jchess.server.generated.ErrorType;
import net.jcom.jchess.server.generated.JChessMessage;
import net.jcom.jchess.server.iostreams.JChessInputStream;
import net.jcom.jchess.server.iostreams.JChessOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.jcom.jchess.server.StartServer.logger;

public class Communicator {
    private static final ExecutorService loginQueueHandler = Executors.newFixedThreadPool(1);
    private JChessInputStream fromPlayer;
    private JChessOutputStream toPlayer;

    private Player player;

    private final Object lock = new Object();


    public Communicator(Socket socket) {
        try {
            this.fromPlayer = new JChessInputStream(socket.getInputStream());
        } catch (IOException var4) {
            logger.error("Could not open InputStream", var4);
        }
        try {
            this.toPlayer = new JChessOutputStream(socket.getOutputStream());
        } catch (IOException var3) {
            logger.error("Could not open Output Stream", var3);
        }
    }

    public JChessMessage receiveMessage() {
        try {
            return this.fromPlayer.readJChess();
        } catch (InvalidSchemaVersion e) {
            logger.info("Client sent a message not compatible with the" +
                    " current version of the server", e);
            this.sendMessage(JChessMessageFactory.createDisconnectMessage(this.player != null ?
                            this.player.getId() :
                            Defaults.DEFAULT_PLAYER.getId(),
                    ErrorType.VERSION_MISMATCH));
            Server.getInstance().removePlayer(this);
        } catch (IOException var2) {
            logger.info("Connection was closed unexpected", var2);
            Server.getInstance().removePlayer(this);
        }
        return null;
    }

    public boolean sendMessage(JChessMessage message) {
        synchronized (this.lock) {
            try {
                return this.toPlayer.write(message);
            } catch (SocketException var5) {
                logger.info("Client is not reachable.");
            } catch (IOException var6) {
                logger.error(var6.getLocalizedMessage(), var6);
            }
            return false;
        }
    }

    public void sendMessageOrRemove(JChessMessage message) {
        synchronized (this.lock) {
            try {
                if (!this.toPlayer.write(message)) {
                    logger.info("Client is not reachable.");
                    Server.getInstance().removePlayer(this);
                }
            } catch (SocketException var5) {
                logger.info("Client is not reachable.");
                Server.getInstance().removePlayer(this);
            } catch (IOException var6) {
                logger.error(var6.getLocalizedMessage(), var6);
                Server.getInstance().removePlayer(this);
            }
        }
    }

    public Player login() {
        //Startet Login Routine
        var playerFuture = loginQueueHandler.submit(new PlayerLoginCallable(this));
        Player player = null;
        try {
            player = playerFuture.get();
        } catch (ExecutionException | InterruptedException var4) {
            logger.error(var4.getLocalizedMessage(), var4);
        }
        this.player = player;
        return player;
    }

    public void sendDisconnectAndKick(JChessMessage disconnectMessage) {
        this.sendMessage(disconnectMessage);
        this.close();
    }

    public void close() {
        try {
            this.fromPlayer.close();
            this.toPlayer.close();
        } catch (IOException var2) {
            logger.error(var2.getLocalizedMessage(), var2);
        }
        Server.getInstance().removePlayer(this);
        logger.info("Removed");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Communicator that = (Communicator) o;

        if (!Objects.equals(this.fromPlayer, that.fromPlayer)) return false;
        return Objects.equals(this.toPlayer, that.toPlayer);
    }

    @Override
    public int hashCode() {
        int result = this.fromPlayer != null ? this.fromPlayer.hashCode() : 0;
        result = 31 * result + (this.toPlayer != null ? this.toPlayer.hashCode() : 0);
        return result;
    }
}
