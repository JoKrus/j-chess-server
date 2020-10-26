package de.djgames.jonas.jcom2.server.logging;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Class to log actions. Currently implemented to create log files
 */
public class Logger {

    public static final LogLevel minLevel = LogLevel.DEBUG;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final String CONSOLE_STRING_TEMPLATE = "[%s] - %s - %s";

    private static void log(Object msg, Throwable throwable, LogLevel lvl) {
        String message = String.format(CONSOLE_STRING_TEMPLATE, DATE_FORMAT.format(new Date()), StringUtils.center(lvl.toString(), LogLevel.maxNameLen), msg);
        if (lvl.getVal() < minLevel.getVal()) {
            return;
        }
        System.out.println(message);
        if (throwable != null)
            throwable.printStackTrace();
    }


    public static void debug(Object msg) {
        log(msg, null, LogLevel.DEBUG);
    }

    public static void debug(Object msg, Throwable throwable) {
        log(msg, throwable, LogLevel.DEBUG);
    }

    public static void info(Object msg) {
        log(msg, null, LogLevel.INFO);
    }

    public static void info(Object msg, Throwable throwable) {
        log(msg, throwable, LogLevel.INFO);
    }

    public static void warning(Object msg) {
        log(msg, null, LogLevel.WARNING);
    }

    public static void warning(Object msg, Throwable throwable) {
        log(msg, throwable, LogLevel.WARNING);
    }

    public static void error(Object msg) {
        log(msg, null, LogLevel.ERROR);
    }

    public static void error(Object msg, Throwable throwable) {
        log(msg, throwable, LogLevel.ERROR);
    }

    public static void fatal(Object msg) {
        log(msg, null, LogLevel.FATAL);
    }

    public static void fatal(Object msg, Throwable throwable) {
        log(msg, throwable, LogLevel.FATAL);
    }

    public enum LogLevel {
        DEBUG(0), INFO(1), WARNING(2), ERROR(3), FATAL(4), STACK(3);

        static int maxNameLen = getMaxNameLength();
        private final int val;

        LogLevel(int val) {
            this.val = val;
        }

        private static int getMaxNameLength() {
            int ret = 0;
            for (LogLevel value : LogLevel.values()) {
                ret = Math.max(ret, value.toString().length());
            }
            return ret;
        }

        public int getVal() {
            return val;
        }
    }
}
