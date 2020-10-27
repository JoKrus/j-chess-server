package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.GameServer;
import de.djgames.jonas.jcom2.server.generated.ErrorType;
import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.logging.Logger;

import javax.xml.bind.UnmarshalException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Connection {
    private final ExecutorService executor;
    private final Socket socket;
    private Future<Client> clientFuture;
    private XmlInputStream inFromClient;
    private XmlOutputStream outToClient;
    private UUID id;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            this.inFromClient = new XmlInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            Logger.error("Connection.couldNotOpenInputStream");
        }
        try {
            this.outToClient = new XmlOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            Logger.error("Connection.couldNotOpenOutputStream");
        }

        this.executor = Executors.newFixedThreadPool(4);
    }

    public Client getClient() throws ExecutionException, InterruptedException {
        if (!clientFuture.isDone()) return null;
        return clientFuture.get();
    }

    // TODO Muss nach Login unbedingt gesetzt werden!!!!!!!
    // SEHR UNSCHOEN!!!!
    public void setId(UUID id) {
        this.id = id;
    }

    public InetAddress getIpAddress() {
        return socket.getInetAddress();
    }

    /**
     * Allgemeines Senden einer fertigen JComMessage-Instanz
     */
    public void sendMessage(JComMessage jComMessage, boolean withTimer) {
        // Timer starten, der beim lesen beendet wird
        // Ablauf Timer = Problem User
        if (withTimer) {
            //   this.timeOutManager.startSendMessageTimeOut(this.id, this);
        }
        try {
            jComMessage.setId(this.id.toString());
            this.outToClient.write(jComMessage);
        } catch (IOException e) {
            Logger.info("Connection.playerExitedUnexpected");
            // entfernen des Spielers
            GameServer.getInstance().removeConnection(this);
        }
    }

    /**
     * Conveniance, ruft sendMessage(message,false) auf
     *
     * @param message Nachricht die übermittelt werden soll
     */
    public void sendMessage(JComMessage message) {
        sendMessage(message, false);
    }


    /**
     * Allgemeines empfangen einer JComMessage-Instanz
     *
     * @return eingelesenes JComMessage
     */
    public JComMessage receiveMessage() {
        JComMessage result = null;
        try {
            result = this.inFromClient.readJCom();
        } catch (UnmarshalException e) {
            Logger.info("Connection.XmlError", e);
        } catch (IOException e) {
            Logger.info("Connection.playerExitedUnexpected");
            // entfernen des Spielers
            GameServer.getInstance().removeConnection(this);
        } catch (OutOfMemoryError e) {
            Logger.error("Connection.MemoryLeak");
            // TODO Wenn zulange Nachrichten geschickt werden
            GameServer.getInstance().removeConnection(this);
        } catch (IllegalArgumentException e) {
            Logger.error("Connection.NegativeMessageSize");
            // TODO Wenn negative Zahl als Länge geschickt wird
            GameServer.getInstance().removeConnection(this);
        }
        return result;
    }

    /**
     * Allgemeines Erwarten eines Login
     *
     * @return Neuer Client, bei einem Fehler jedoch null
     */
    public Future<Client> login() {
        // ein Thread fuer einen Login. Weitere Logins in rufendender Schleife
        clientFuture = executor.submit(new LoginTask(this));
        return clientFuture;
    }

    /**
     * Senden, dass Spieler diconnected wurde
     */
    public void disconnect(ErrorType errortype) {
        try {
            Future<Client> clientFuture = this.clientFuture;
            String name;
            if (clientFuture.isDone()) {
                name = clientFuture.get().getName();
            } else {
                name = "<not logged in>";
            }
            //   this.sendMessage(JComMessageFactory.createDisconnectMessage(this.id, name, errortype), false);

        } catch (InterruptedException |
                ExecutionException e) {
            Logger.error("Connection.LoginInterrupted");
        }
        terminateConnection();
    }


    private void terminateConnection() {
        try {
            this.inFromClient.close();
            this.outToClient.close();
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GameServer.getInstance().removeConnection(this);
        Logger.info(socket.getInetAddress() + " wurde entfernt");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return id == that.id &&
                Objects.equals(socket, that.socket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket, id);
    }
}
