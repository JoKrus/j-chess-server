package de.djgames.jonas.jcom2.server;

import de.djgames.jonas.jcom2.server.logging.Logger;
import de.djgames.jonas.jcom2.server.networking.Client;
import de.djgames.jonas.jcom2.server.networking.Connection;
import de.djgames.jonas.jcom2.server.networking.TCPConnectionCreationTask;
import de.djgames.jonas.jcom2.server.settings.Settings;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static de.djgames.jonas.jcom2.server.networking.Defaults.DEFAULT_UUID;

public class GameServer {
    private static final GameServer instance = new GameServer();

    //Erstmal nur ohne ssl
    private ServerSocket serverSocket;

    //Erstmal ohne Spectators
    private List<Future<Client>> connectedClients;

    private List<String> connectedIPs;

    private GameServer() {
        try {
            serverSocket = new ServerSocket(Settings.PORT);

        } catch (IOException e) {
            Logger.fatal("Server can't be started");
        }
        connectedClients = new ArrayList<>();
        connectedIPs = new ArrayList<>();
    }

    public static GameServer getInstance() {
        return instance;
    }

    /**
     * Auf TCP Verbindungen warten und den Spielern die Verbindung ermoeglichen
     * Speichert alle eincommenden Verbindungen in connectedClients
     * Spieler werden in prepareGame gefiltert
     */
    public void waitForConnections() {
        try {
            // unverschluesselt
            serverSocket = new ServerSocket(Settings.PORT);
        } catch (IOException e) {
            Logger.info("Game.portUsed");
        }
        connectedIPs = new ArrayList<>();

        final CyclicBarrier barrier = new CyclicBarrier(2);
        Socket clientSocket = null;
        TCPConnectionCreationTask waitForConnectionTask = new TCPConnectionCreationTask(serverSocket, barrier);
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<Socket> noSSLSocketFuture = null;

        printServerAddresses(serverSocket);
        while (true) {
            try {
                cleanUpConnections();
                clientSocket = null;
                Logger.info("Game.waitingForConnections");
                // Neustart des benutzten serverSockets
                //barrier.reset(); rausnehmen wegen Exception?
                if (noSSLSocketFuture == null || noSSLSocketFuture.isDone()) {
                    noSSLSocketFuture = pool.submit(waitForConnectionTask);
                }

                // Warten bis Verbindung kommt
                barrier.await();
                cleanUpConnections();

                //TODO Zeug machen mit timeout

                // An dieser Stelle existiert (oder existiert in unmittelbarer Zukunft) ein neuer Socket, weil ein Client verbunden wurde
                // Nach der ersten Verbindung soll der Timeout gestartet werden
//                if (game != null && game.getTournamentStatus() == Tournament.TournamentStatus.WAIT_FOR_PLAYERS) {
//                    // TODO: Check: wenn sich kein Spieler verbindet, sollte ewig gewartet werden
//                    // Es wird noch nicht geprüft ob es sich bei der eingehenden Verbindung um einen Spieler handelt
//                    game.getTimeOutManager().startLoginTimeOut();
//                }
                try {
                    // Fuer megaeklige Racekondition
                    // Falls TCPConnectionCreationTask.call:barrier.await() langsamer ist als hier nach der Barrier von oben
                    boolean fuckRaceConditions = true;
                    while (fuckRaceConditions) {
                        // Abrufen des Sockets für die neue Verbindung
                        if (noSSLSocketFuture.isDone()) {
                            fuckRaceConditions = false;
                            clientSocket = noSSLSocketFuture.get();
                        }
                    }
                } catch (ExecutionException e) {
                    Logger.error(e.getMessage(), e);
                }
                if (clientSocket != null) {
                    // Nur eine Verbindung pro IP erlauben (Ausnahme localhost)
                    InetAddress inetAddress = clientSocket.getInetAddress();
                    String ip = inetAddress.getHostAddress();
                    if (!connectedIPs.contains(ip)) {
                        Connection connection = new Connection(clientSocket);

                        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        // Verbindung aufgebaut, zuerst login durchführen
                        // asynchron
                        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                        connectedClients.add(connection.login());
                    } else {
                        Logger.info("Game.HostAlreadyConnected " + ip);
                    }
                } else
                    System.out.println("jClientSocket==null");
            } catch (InterruptedException e) {
                Logger.info("Game.playerWaitingTimedOut");
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    public void addConnectedIP(String ip) {
        connectedIPs.add(ip);
    }

    public void cleanUpConnections() {
        this.removeConnection(null);
    }

    public void removeConnection(Connection toRemove) {
        try {
            boolean removed;
            // Nicht waehrend Iteration Elemente rausloeschen!
            // Finden des zu loeschenden Clients
            List<Future<Client>> clientsToBeRemoved = connectedClients.stream()
                    .filter(Future::isDone)
                    .filter((client) -> {
                        try {
                            return (client.get().getId() == DEFAULT_UUID) || (client.get().getConnectionToClient().equals(toRemove));
                        } catch (InterruptedException | ExecutionException e) {
                            //remove interupted Logins to
                            return true;
                        }
                    }).collect(Collectors.toList());

            // Tatsaechliches Loeschen des Clients
            for (Future<Client> clientToBeRemoved : clientsToBeRemoved) {
                removed = connectedClients.remove(clientToBeRemoved);
                if (removed) {
                    if (clientToBeRemoved.isDone()) {
                        Client client = clientToBeRemoved.get();
                        if (client != null) {
                            connectedIPs.remove(client.getConnectionToClient().getIpAddress().getHostAddress());
                            //TODO remove von Game objekt
                        }
                    }
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            Logger.error("Exception in GameServer.removeConnection", e);
        }
    }


    private void printServerAddresses(ServerSocket serverSocket) {
        int serverPort = serverSocket.getLocalPort();
        // Server verwenden die Adresse 0.0.0.0 oft als Platzhalter für alle IP-Adressen des
        // lokalen Computers. Wenn der Server zwei IP-Adressen hat, z. B. 192.168.5.10
        // und 10.17.0.12, und der Server auf diese Adresse reagiert, ist er auf beiden
        // IP-Adressen erreichbar.
        // https://de.wikipedia.org/wiki/0.0.0.0
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            Logger.error(e.getMessage(), e);
        }
        if (networkInterfaces == null) throw new RuntimeException("Could not get NetworkInterfaces");

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface n = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAdresses = n.getInetAddresses();
            while (inetAdresses.hasMoreElements()) {
                InetAddress inetAddress = inetAdresses.nextElement();
                // nur die IPv4-Adressen ausgeben
                if (inetAddress instanceof Inet4Address)
                    Logger.info(String.format("Server listening on %s:%d", inetAddress.getHostAddress(), serverPort));
            }
        }
    }
}
