package net.jcom.jchess.server.logging;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Logger {
    private static final String MESSAGE_TEMPLATE = "|%s| - %s - %s%n";

    public LoggingLevel minimumLevel;
    private final ArrayList<LoggerBuilder.WriteObject> writeObjects;
    private final DateFormat format;

    Logger(ArrayList<LoggerBuilder.WriteObject> writeObjects, LoggingLevel minimumLevel, DateFormat format) {
        this.writeObjects = writeObjects;
        this.minimumLevel = minimumLevel;
        this.format = format;
    }

    private void log(Object msg, Throwable throwable, LoggingLevel loggingLevel) {
        if (loggingLevel.getSeverity() < this.minimumLevel.getSeverity()) {
            return;
        }
        String message = String.format(MESSAGE_TEMPLATE,
                this.format.format(Date.from(Instant.now())), StringUtils.center(loggingLevel.toString(),
                        LoggingLevel.MAX_NAME_LENGTH), msg);
        for (var wObj : this.writeObjects) {
            try {
                IOUtils.write(message, wObj.getMessageOutputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                //cant log error if error happens in logging
                e.printStackTrace();
            }

            if (throwable != null) {
                try {
                    IOUtils.write("Logged exception:" + System.lineSeparator(), wObj.getErrorOutputStream(), StandardCharsets.UTF_8);
                    throwable.printStackTrace(new PrintStream(wObj.getErrorOutputStream()));
                } catch (IOException e) {
                    //cant log error if error happens in logging
                    e.printStackTrace();
                }
            }
        }
    }

    public void debug(Object msg) {
        log(msg, null, LoggingLevel.DEBUG);
    }

    public void debug(Object msg, Throwable throwable) {
        log(msg, throwable, LoggingLevel.DEBUG);
    }

    public void info(Object msg) {
        log(msg, null, LoggingLevel.INFO);
    }

    public void info(Object msg, Throwable throwable) {
        log(msg, throwable, LoggingLevel.INFO);
    }

    public void warning(Object msg) {
        log(msg, null, LoggingLevel.WARNING);
    }

    public void warning(Object msg, Throwable throwable) {
        log(msg, throwable, LoggingLevel.WARNING);
    }

    public void error(Object msg) {
        log(msg, null, LoggingLevel.ERROR);
    }

    public void error(Object msg, Throwable throwable) {
        log(msg, throwable, LoggingLevel.ERROR);
    }

    public void fatal(Object msg) {
        log(msg, null, LoggingLevel.FATAL);
    }

    public void fatal(Object msg, Throwable throwable) {
        log(msg, throwable, LoggingLevel.FATAL);
    }


    public enum LoggingLevel {
        DEBUG(0),
        INFO(1),
        WARNING(2),
        ERROR(3),
        FATAL(4),
        OFF(5);

        private final int severity;
        static final int MAX_NAME_LENGTH = getMaxNameLength();

        LoggingLevel(int severity) {
            this.severity = severity;
        }

        private static int getMaxNameLength() {
            return Arrays.stream(LoggingLevel.values()).mapToInt(value -> value.name().length()).max().orElse(1);
        }

        public int getSeverity() {
            return this.severity;
        }
    }
}
