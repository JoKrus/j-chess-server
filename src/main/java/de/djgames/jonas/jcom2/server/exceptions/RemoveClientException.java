package de.djgames.jonas.jcom2.server.exceptions;

public class RemoveClientException extends RuntimeException {
    public RemoveClientException(String message, Throwable e) {
        super(message, e);
    }
}
