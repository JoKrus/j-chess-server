package de.djgames.jonas.jcom2.server.networking;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class StringInputStream {

    private final InputStream inputStream;

    public StringInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String readMessage() throws IOException {
        int messageLength = readHeader();
        byte[] message = new byte[messageLength];
        IOUtils.read(inputStream, message);
        return new String(message, StandardCharsets.UTF_8);
    }

    private int readHeader() throws IOException {
        byte[] textLength = new byte[4];
        IOUtils.read(inputStream, textLength);
        return new BigInteger(textLength).intValue();
    }

    public void close() throws IOException {
        this.inputStream.close();
    }
}