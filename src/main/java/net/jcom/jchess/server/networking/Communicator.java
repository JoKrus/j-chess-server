package net.jcom.jchess.server.networking;

import net.jcom.jchess.server.Server;
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
    private final static ExecutorService loginQueueHandler = Executors.newFixedThreadPool(1);

    private JChessInputStream fromPlayer;
    private JChessOutputStream toPlayer;

    private final Object lock = new Object();


    public Communicator(Socket socket) {
        try {
            this.fromPlayer = new JChessInputStream(socket.getInputStream());
        } catch (IOException e) {
            logger.error("Could not open InputStream", e);
        }
        try {
            this.toPlayer = new JChessOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            logger.error("Could not open Output Stream", e);
        }
    }

    public JChessMessage receiveMessage() {
        try {
            return this.fromPlayer.readJChess();
        } catch (IOException e) {
            logger.info("Connection was closed unexpected", e);
            Server.getInstance().removePlayer(this);
        }
        return null;
    }

    //true success
    public boolean sendMessage(JChessMessage message) {
        synchronized (this.lock) {
            try {
                return this.toPlayer.write(message);
            } catch (SocketException e) {
                logger.info("Client is not reachable.");
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
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
            } catch (SocketException e) {
                logger.info("Client is not reachable.");
                Server.getInstance().removePlayer(this);
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
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
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return player;
    }

    public void close() {
        try {
            this.fromPlayer.close();
            this.toPlayer.close();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
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
