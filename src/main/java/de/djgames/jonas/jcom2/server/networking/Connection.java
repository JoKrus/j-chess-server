package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.GameServer;
import de.djgames.jonas.jcom2.server.exceptions.RemoveClientException;
import de.djgames.jonas.jcom2.server.generated.ErrorType;
import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.logging.Logger;

import javax.xml.bind.UnmarshalException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Connection {
    private final static ExecutorService loginQueueHandler = Executors.newFixedThreadPool(4);

    public final Socket socket;
    private Future<Client> clientFuture;
    private XmlInputStream fromClient;
    private XmlOutputStream toClient;
    private UUID id;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            this.fromClient = new XmlInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            Logger.error("Could not open InputStream", e);
        }
        try {
            this.toClient = new XmlOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            Logger.error("Could not open Output Stream", e);
        }
    }

    public Client getClient() throws ExecutionException, InterruptedException {
        if (!clientFuture.isDone()) return null;
        return clientFuture.get();
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InetAddress getIpAddress() {
        return socket.getInetAddress();
    }

    public void sendMessage(JComMessage jComMessage, boolean withTimer) {
        if (withTimer) {
            //   this.timeOutManager.startSendMessageTimeOut(this.id, this);
        }
        try {
            jComMessage.setId(this.id.toString());
            this.toClient.write(jComMessage);
        } catch (SocketException e) {
            Logger.info("Connection was closed unexpected", e);
            GameServer.getInstance().removeConnection(this);
            throw new RemoveClientException(e.getMessage(), e);
        } catch (IOException e2) {
            Logger.info("Connection was closed unexpected", e2);
            GameServer.getInstance().removeConnection(this);
        }
    }

    public void sendMessage(JComMessage message) {
        sendMessage(message, false);
    }

    public JComMessage receiveMessage() {
        JComMessage result = null;
        try {
            result = this.fromClient.readJCom();
        } catch (UnmarshalException e) {
            Logger.info(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            Logger.info("Connection was closed unexpected", e);
            GameServer.getInstance().removeConnection(this);
        } catch (OutOfMemoryError e) {
            Logger.error(e.getLocalizedMessage(), e);
            GameServer.getInstance().removeConnection(this);
        } catch (IllegalArgumentException e) {
            Logger.error("Message length can't be negative");
            GameServer.getInstance().removeConnection(this);
        }
        return result;
    }

    public Future<Client> login() {
        // ein Thread fuer einen Login. Weitere Logins in rufendender Schleife
        clientFuture = loginQueueHandler.submit(new LoginTask(this));
        return clientFuture;
    }

    public void disconnect(ErrorType errortype) {
        try {
            Future<Client> clientFuture = this.clientFuture;
            String name;
            if (clientFuture.isDone()) {
                name = clientFuture.get().getName();
            } else {
                name = "<not logged in>";
            }
            this.sendMessage(JComMessageFactory.createDisconnectMessage(this.id, name, errortype), false);
        } catch (InterruptedException | ExecutionException e) {
            Logger.error("Connection.LoginInterrupted");
        }
        closeConnection();
    }

    private void closeConnection() {
        try {
            this.fromClient.close();
            this.toClient.close();
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
