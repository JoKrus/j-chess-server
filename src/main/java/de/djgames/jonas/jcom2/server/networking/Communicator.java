package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.Server;
import de.djgames.jonas.jcom2.server.generated.JComMessage;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.djgames.jonas.jcom2.server.StartServer.logger;

public class Communicator {
    private final static ExecutorService loginQueueHandler = Executors.newFixedThreadPool(4);

    private JComInputStream fromPlayer;
    private JComOutputStream toPlayer;

    public Communicator(Socket socket) {
        try {
            this.fromPlayer = new JComInputStream(socket.getInputStream());
        } catch (IOException e) {
            logger.error("Could not open InputStream", e);
        }
        try {
            this.toPlayer = new JComOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            logger.error("Could not open Output Stream", e);
        }
    }

    public JComMessage receiveMessage() {
        try {
            return this.fromPlayer.readJCom();
        } catch (IOException e) {
            logger.info("Connection was closed unexpected", e);
            Server.getInstance().removePlayer(this);
        }
        return null;
    }

    //true success
    public boolean sendMessage(JComMessage message) {
        try {
            return this.toPlayer.write(message);
        } catch (SocketException e) {
            logger.info("Client is not reachable.");
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return false;
    }

    public void sendMessageOrRemove(JComMessage message) {
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
