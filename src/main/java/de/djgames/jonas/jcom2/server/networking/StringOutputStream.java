package de.djgames.jonas.jcom2.server.networking;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class StringOutputStream {

    private final OutputStream outputStream;

    public StringOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeString(String text) throws IOException {
        byte[] header = new BigInteger(Integer.toString(text.length())).toByteArray();
        //header does not return 4 bytes
        byte[] headerToSend = new byte[4];
        System.arraycopy(header, 0, headerToSend, 4 - header.length, header.length);

        IOUtils.write(headerToSend, outputStream);
        IOUtils.write(text, outputStream, StandardCharsets.UTF_8);
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void close() throws IOException {
        this.outputStream.close();
    }
}
