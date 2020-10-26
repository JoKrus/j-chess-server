package de.djgames.jonas.jcom2.server.networking;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class UTFOutputStream {

    private final OutputStream outputStream;

    public UTFOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void writeUTF8(String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        // Transform to network order
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putInt(bytes.length);
        outputStream.write(byteBuffer.array());
        outputStream.write(bytes);
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    public void close() throws IOException {
        this.outputStream.close();
    }
}
