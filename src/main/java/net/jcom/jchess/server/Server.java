package net.jcom.jchess.server;

import net.jcom.jchess.server.factory.JChessMessageFactory;
import net.jcom.jchess.server.logic.Match;
import net.jcom.jchess.server.networking.Communicator;
import net.jcom.jchess.server.networking.Player;
import net.jcom.jchess.server.networking.PlayerStatus;
import net.jcom.jchess.server.settings.Settings;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static net.jcom.jchess.server.StartServer.logger;
import static net.jcom.jchess.server.networking.Defaults.DEFAULT_UUID;

public class Server {
    private static final Server instance = new Server();

    //Erstmal nur ohne ssl
    private ServerSocket serverSocket;

    //Erstmal ohne Spectators
    private final List<Player> connectedPlayers;

    //private final ExecutorService runningGamePoolExecutor = Executors.newWorkStealingPool();

    private Server() {
        try {
            this.serverSocket = new ServerSocket(Settings.getInt(Settings.PORT));
        } catch (IOException e) {
            logger.fatal("Server cannot be started", e);
            System.exit(1);
        }
        this.connectedPlayers = new ArrayList<>();

        ScheduledThreadPoolExecutor heartBeatSender = new ScheduledThreadPoolExecutor(1);
        heartBeatSender.scheduleAtFixedRate(this::heartBeatSender, 1, 15, TimeUnit.SECONDS);

        ScheduledThreadPoolExecutor matchMaker = new ScheduledThreadPoolExecutor(1);
        matchMaker.scheduleAtFixedRate(this::matchMaker, 0, 3, TimeUnit.SECONDS);
    }

    private void matchMaker() {
        logger.debug("Start matchmaking cycle");
        var queueingPlayers =
                this.connectedPlayers.stream().filter(player -> player.getStatus() == PlayerStatus.QUEUE)
                        .collect(Collectors.toList());
        //TODO to ensure, player 3 also gets matched at some point, maybe take queue time into account
        Collections.shuffle(this.connectedPlayers);
        if (queueingPlayers.size() % 2 == 1) queueingPlayers.remove(queueingPlayers.size() - 1);

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
        logger.debug("Start heartbeat cycle");
        try {
            this.connectedPlayers.removeIf(player ->
                    !player.getCommunicator().sendMessage(JChessMessageFactory.createHeartbeatMessage(player.getId())));
        } catch (Throwable t) {
            logger.fatal(t.getLocalizedMessage(), t);
        }
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
                    cleanUpPlayers();
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
                (player == null || player.getId() == DEFAULT_UUID) || (player.getCommunicator().equals(toRemove)));
    }

    public List<String> getConnectedPlayerNames() {
        return this.connectedPlayers.stream().map(Player::getPlayerName).collect(Collectors.toList());
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
