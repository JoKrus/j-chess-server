package de.djgames.jonas.jcom2.server;

import de.djgames.jonas.jcom2.server.networking.Communicator;
import de.djgames.jonas.jcom2.server.networking.JComMessageFactory;
import de.djgames.jonas.jcom2.server.networking.Player;
import de.djgames.jonas.jcom2.server.settings.Settings;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static de.djgames.jonas.jcom2.server.StartServer.logger;
import static de.djgames.jonas.jcom2.server.networking.Defaults.DEFAULT_UUID;

public class Server {
    private static final Server instance = new Server();

    //Erstmal nur ohne ssl
    private ServerSocket serverSocket;

    //Erstmal ohne Spectators
    private final List<Player> connectedPlayers;

    private Server() {
        try {
            this.serverSocket = new ServerSocket(Settings.getInt(Settings.PORT));
        } catch (IOException e) {
            logger.fatal("Server cannot be started", e);
            System.exit(1);
        }
        this.connectedPlayers = new ArrayList<>();

        //TODO only send to people not currently playing
        ScheduledThreadPoolExecutor heartBeatSender = new ScheduledThreadPoolExecutor(1);
        heartBeatSender.scheduleAtFixedRate(() -> {
            try {
                this.connectedPlayers.removeIf(player ->
                        !player.getCommunicator().sendMessage(JComMessageFactory.createHeartbeatMessage(player.getId())));
            } catch (Throwable t) {
                logger.fatal(t.getLocalizedMessage(), t);
            }
        }, 0, 15, TimeUnit.SECONDS);

    }

    public static Server getInstance() {
        return instance;
    }

    public void waitForConnections() {
        printServerAddresses(this.serverSocket);

        while (true) {
            try {
                cleanUpPlayers();
                logger.info("Waiting for a connection");
                Socket clientSocket = this.serverSocket.accept();
                cleanUpPlayers();
                if (clientSocket != null) {
                    Communicator communicator = new Communicator(clientSocket);
                    this.connectedPlayers.add(communicator.login());
                    logger.info(this.connectedPlayers.size() + " clients connected");
                } else {
                    logger.info("client socket is null");
                }
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
    }

    public void cleanUpPlayers() {
        //removes all clients with a null communicator
        this.removePlayer(null);
    }

    public void removePlayer(Communicator toRemove) {
        this.connectedPlayers.removeIf(player ->
                (player.getId() == DEFAULT_UUID) || (player.getCommunicator().equals(toRemove)));
    }


    private void printServerAddresses(ServerSocket serverSocket) {
        int serverPort = serverSocket.getLocalPort();

        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            logger.error(e.getMessage(), e);
        }
        if (networkInterfaces == null) throw new RuntimeException("Could not get NetworkInterfaces");

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface n = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAdresses = n.getInetAddresses();
            while (inetAdresses.hasMoreElements()) {
                InetAddress inetAddress = inetAdresses.nextElement();
                if (inetAddress instanceof Inet4Address)
                    logger.info(String.format("Server listening on %s:%d", inetAddress.getHostAddress(), serverPort));
            }
        }
    }
}

