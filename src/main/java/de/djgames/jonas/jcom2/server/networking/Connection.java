package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.GameServer;
import de.djgames.jonas.jcom2.server.exceptions.RemoveClientException;
import de.djgames.jonas.jcom2.server.generated.ErrorType;
import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.networking_own.JComInputStream;
import de.djgames.jonas.jcom2.server.networking_own.JComOutputStream;

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

import static de.djgames.jonas.jcom2.server.StartServer.logger;

public class Connection {
    private final static ExecutorService loginQueueHandler = Executors.newFixedThreadPool(4);

    public final Socket socket;
    private Future<Client> clientFuture;
    private JComInputStream fromClient;
    private JComOutputStream toClient;
    private UUID id;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            this.fromClient = new JComInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            logger.error("Could not open InputStream", e);
        }
        try {
            this.toClient = new JComOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            logger.error("Could not open Output Stream", e);
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

    /**
     * @param jComMessage    m
     * @param withTimer      wT
     * @param surpressRemove removed already oder wirft excep
     * @throws RemoveClientException wenn sR false und fehler kam
     */
    public void sendMessage(JComMessage jComMessage, boolean withTimer, boolean surpressRemove) throws RemoveClientException {
        if (withTimer) {
            //   this.timeOutManager.startSendMessageTimeOut(this.id, this);
        }
        try {
            jComMessage.setId(this.id.toString());
            this.toClient.write(jComMessage);
        } catch (SocketException e) {
            logger.info("Connection was closed unexpected");
            if (surpressRemove) {
                GameServer.getInstance().removeConnection(this);
            } else {
                throw new RemoveClientException(e.getMessage(), e);
            }
        } catch (IOException e2) {
            logger.info("Connection was closed unexpected", e2);
            if (surpressRemove) {
                GameServer.getInstance().removeConnection(this);
            } else {
                throw new RemoveClientException(e2.getMessage(), e2);
            }
        }
    }

    public void sendMessage(JComMessage message, boolean withTimer) {
        sendMessage(message, withTimer, false);
    }

    public void sendMessage(JComMessage message) {
        sendMessage(message, false, false);
    }

    public JComMessage receiveMessage() {
        JComMessage result = null;
        try {
            result = this.fromClient.readJCom();
        } catch (UnmarshalException e) {
            logger.info(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            logger.info("Connection was closed unexpected", e);
            GameServer.getInstance().removeConnection(this);
        } catch (OutOfMemoryError e) {
            logger.error(e.getLocalizedMessage(), e);
            GameServer.getInstance().removeConnection(this);
        } catch (IllegalArgumentException e) {
            logger.error("Message length can't be negative");
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
            logger.error("Connection.LoginInterrupted");
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
        logger.info(socket.getInetAddress() + " wurde entfernt");
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
