package de.djgames.jonas.jcom2.server.logging;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class LoggerBuilder {
    private ArrayList<WriteObject> outputStreams = new ArrayList<>();
    private Logger.LoggingLevel minimumLevel = Logger.LoggingLevel.ERROR;
    private DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private LoggerBuilder() {
    }

    public static LoggerBuilder init() {
        return new LoggerBuilder();
    }

    public LoggerBuilder format(DateFormat format) {
        this.format = format;
        return this;
    }

    public LoggerBuilder minimumLevel(Logger.LoggingLevel minimumLevel) {
        this.minimumLevel = minimumLevel;
        return this;
    }

    public LoggerBuilder addWriteStream(OutputStream outputStream) {
        return addWriteStream(outputStream, true);
    }

    public LoggerBuilder addWriteStream(OutputStream outputStream, boolean withErrors) {
        outputStreams.add(new WriteObject(outputStream, withErrors ? outputStream : OutputStream.nullOutputStream()));
        return this;
    }

    public LoggerBuilder addWriteStream(OutputStream messageOS, OutputStream errorOS) {
        outputStreams.add(new WriteObject(messageOS, errorOS));
        return this;
    }

    public Logger build() {
        return new Logger(outputStreams, minimumLevel, format);
    }

    public static class WriteObject {
        private final OutputStream messageOutputStream;
        private final OutputStream errorOutputStream;

        public WriteObject(OutputStream messageOutputStream, OutputStream errorOutputStream) {
            this.messageOutputStream = messageOutputStream;
            this.errorOutputStream = errorOutputStream;
        }

        public OutputStream getMessageOutputStream() {
            return messageOutputStream;
        }

        public OutputStream getErrorOutputStream() {
            return errorOutputStream;
        }
    }
}
