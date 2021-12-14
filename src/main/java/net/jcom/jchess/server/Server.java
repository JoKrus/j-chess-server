package net.jcom.jchess.server;

import net.jcom.jchess.server.factory.JChessMessageFactory;
import net.jcom.jchess.server.logic.Match;
import net.jcom.jchess.server.networking.Communicator;
import net.jcom.jchess.server.networking.Player;
import net.jcom.jchess.server.networking.PlayerStatus;
import net.jcom.jchess.server.settings.Settings;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.jcom.jchess.server.StartServer.logger;
import static net.jcom.jchess.server.networking.Defaults.DEFAULT_UUID;

public class Server {
    private static final Server instance = new Server();

    //Erstmal nur ohne ssl
    private ServerSocket serverSocket;
    private ServerSocket sslServerSocket;

    //Erstmal ohne Spectators
    private final List<Player> connectedPlayers;

    //private final ExecutorService runningGamePoolExecutor = Executors.newWorkStealingPool();

    private Server() {
        try {
            this.serverSocket = new ServerSocket(Settings.getInt(Settings.PORT));
            this.sslServerSocket = SSLServerSocketFactory.getDefault().createServerSocket(Settings.getInt(Settings.SSL_PORT));
        } catch (IOException e) {
            logger.fatal("Server cannot be started", e);
            System.exit(1);
        }
        this.connectedPlayers = Collections.synchronizedList(new ArrayList<>());

        ScheduledThreadPoolExecutor heartBeatSender = new ScheduledThreadPoolExecutor(1);
        heartBeatSender.scheduleAtFixedRate(this::heartBeatSender, 1, 15, TimeUnit.SECONDS);

        ScheduledThreadPoolExecutor matchMaker = new ScheduledThreadPoolExecutor(1);
        matchMaker.scheduleAtFixedRate(this::matchMaker, 0, 25, TimeUnit.SECONDS);
    }

    private void matchMaker() {
        List<Player> queueingPlayers = new ArrayList<>();
        this.connectedPlayers.forEach(player -> {
            if (player.getStatus() == PlayerStatus.QUEUE) {
                queueingPlayers.add(player);
            }
        });

        //TODO to ensure, player 3 also gets matched at some point, maybe take queue time into account
        Collections.shuffle(queueingPlayers);
        if (queueingPlayers.size() % 2 == 1) {
            queueingPlayers.remove(queueingPlayers.size() - 1);
        }

        List<List<Player>> soonToBeMatches = new ArrayList<>();
        final int MATCH_SIZE = 2;
        for (int i = 0; i < queueingPlayers.size(); i += MATCH_SIZE) {
            soonToBeMatches.add(queueingPlayers.subList(i, i + MATCH_SIZE));
        }

        for (var soonToBeMatch : soonToBeMatches) {
            UUID matchId = UUID.randomUUID();
            Thread t = new Thread(() -> {
                Match match = new Match(soonToBeMatch, matchId);
                match.startMatch();
            });
            t.setName(matchId.toString());
            t.start();
            //  this.runningGamePoolExecutor.submit();
        }
    }

    private void heartBeatSender() {
        try {
            this.connectedPlayers.removeIf((player) ->
                    !player.getCommunicator().sendMessage(JChessMessageFactory.createHeartbeatMessage(player.getId())));
        } catch (Throwable var2) {
            logger.fatal(var2.getLocalizedMessage(), var2);
        }
    }

    public static Server getInstance() {
        return instance;
    }

    public void waitForConnections() {
        this.printServerAddresses(this.serverSocket);
        this.printServerAddresses(this.sslServerSocket);

        Thread servAccept = new Thread(() -> acceptConnections(this.serverSocket));
        Thread sslServAccept = new Thread(() -> acceptConnections(this.sslServerSocket));

        servAccept.start();
        sslServAccept.start();
    }

    private void acceptConnections(ServerSocket servSocket) {
        while (true) {
            try {
                this.cleanUpPlayers();
                logger.info("Waiting for a connection");
                Socket clientSocket = servSocket.accept();
                this.cleanUpPlayers();
                if (clientSocket != null) {
                    Communicator communicator = new Communicator(clientSocket);
                    this.connectedPlayers.add(communicator.login());
                    this.cleanUpPlayers();
                    logger.info(this.connectedPlayers.size() + " clients connected");
                } else {
                    logger.info("client socket is null");
                }
            } catch (IOException var3) {
                logger.error(var3.getLocalizedMessage(), var3);
            }
        }
    }

    public void cleanUpPlayers() {
        //removes all clients with a null communicator
        this.removePlayer(null);
    }

    public void removePlayer(Communicator toRemove) {
        this.connectedPlayers.removeIf((player) ->
                player == null || player.getId() == DEFAULT_UUID || player.getCommunicator().equals(toRemove));
    }

    public List<String> getConnectedPlayerNames() {
        List<String> list = new ArrayList<>();
        this.connectedPlayers.forEach(player -> {
            String playerName = player.getPlayerName();
            list.add(playerName);
        });
        return list;
    }

    private void printServerAddresses(ServerSocket serverSocket) {
        int serverPort = serverSocket.getLocalPort();

        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException var7) {
            logger.error(var7.getMessage(), var7);
        }

        if (networkInterfaces == null) {
            throw new RuntimeException("Could not get NetworkInterfaces");
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface n = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = n.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if (inetAddress instanceof Inet4Address) {
                    logger.info(String.format("Server listening on %s:%d", inetAddress.getHostAddress(), serverPort));
                }
            }
        }
    }
}