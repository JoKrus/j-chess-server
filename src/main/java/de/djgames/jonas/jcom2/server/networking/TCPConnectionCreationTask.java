package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.logging.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;

public class TCPConnectionCreationTask implements Callable<Socket> {
    private final ServerSocket serverSocket;
    private final CyclicBarrier barrier;

    public TCPConnectionCreationTask(ServerSocket serverSocket, CyclicBarrier barrier) {
        this.serverSocket = serverSocket;
        this.barrier = barrier;
    }

    @Override
    public Socket call() {
        Socket incomingSocket = null;
        try {
            incomingSocket = serverSocket.accept();
        } catch (IOException e) {
            Logger.error("Game.errorWhileConnecting" + " (Port:" + serverSocket.getLocalPort() + ")", e);
        }
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            Logger.error("", e);
        }
        if (incomingSocket == null)
            Logger.info("Incoming Socket is null");
        return incomingSocket;
    }
}
