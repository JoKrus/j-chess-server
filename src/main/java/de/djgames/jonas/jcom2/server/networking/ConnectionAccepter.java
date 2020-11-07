package de.djgames.jonas.jcom2.server.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

import static de.djgames.jonas.jcom2.server.StartServer.logger;

public class ConnectionAccepter implements Callable<Socket> {
    private final ServerSocket serverSocket;
    private final CyclicBarrier synchronousBarrier;

    public ConnectionAccepter(ServerSocket serverSocket, CyclicBarrier synchronousBarrier) {
        this.serverSocket = serverSocket;
        this.synchronousBarrier = synchronousBarrier;
    }

    @Override
    public Socket call() {
        Socket incomingSocket = null;
        try {
            incomingSocket = serverSocket.accept();
        } catch (IOException e) {
            logger.error("Game.errorWhileConnecting" + " (Port:" + serverSocket.getLocalPort() + ")", e);
        }
        try {
            synchronousBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            logger.error("", e);
        }
        if (incomingSocket == null)
            logger.info("Incoming Socket is null");
        return incomingSocket;
    }
}
