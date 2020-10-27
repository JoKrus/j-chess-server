package de.djgames.jonas.jcom2.server.settings;

import de.djgames.jonas.jcom2.server.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {
    public static final String S_PORT = "PORT";
    private static final int DEF_PORT = 5123;

    /**
     * Den Detailgrad der Ausgaben festlegen
     */
    public static Logger.LogLevel DEBUG_LEVEL = Logger.minLevel;

    public static final String S_MAX_NAME_LENGTH = "MAX_NAME_LENGTH";
    /**
     * Die maximal erlaubte Laenge des Loginnamens
     */
    private static final int DEF_MAX_NAME_LENGTH = 30;

    public static final String S_LOGINTIMEOUT = "LOGINTIMEOUT";
    /**
     * Die Zeit in Milisekunden, nach der ein Logintimeout eintritt LOGINTIMEOUT =
     * 60000 entspricht einer Minute
     */
    private static final int DEF_LOGINTIMEOUT = 60000;

    public static final String S_LOGINTRIES = "LOGINTRIES";
    private static final int DEF_LOGINTRIES = 3;

    public static final String S_SENDTIMEOUT = "SENDTIMEOUT";
    private static final int DEF_SENDTIMEOUT = 20000;

    public static int PORT = DEF_PORT;
    public static int MAX_NAME_LENGTH = DEF_MAX_NAME_LENGTH;
    public static int LOGINTIMEOUT = DEF_LOGINTIMEOUT;
    public static int LOGINTRIES = DEF_LOGINTRIES;
    public static int SENDTIMEOUT = DEF_SENDTIMEOUT;

    public static void reload(String path) {
        Properties properties = new Properties();
        if (path != null) {
            try (InputStream propStream = new FileInputStream(new File(path))) {
                properties.load(propStream);
            } catch (IOException e) {
                Logger.error("Settings.configNotFound");
            }
        }

        PORT = Integer.parseInt(properties.getProperty(S_PORT, "" + DEF_PORT));
        MAX_NAME_LENGTH = Integer.parseInt(properties.getProperty(S_MAX_NAME_LENGTH, "" + DEF_MAX_NAME_LENGTH));
        LOGINTIMEOUT = Integer.parseInt(properties.getProperty(S_LOGINTIMEOUT, "" + DEF_LOGINTIMEOUT));
        LOGINTRIES = Integer.parseInt(properties.getProperty(S_LOGINTRIES, "" + DEF_LOGINTRIES));
        SENDTIMEOUT = Integer.parseInt(properties.getProperty(S_SENDTIMEOUT, "" + DEF_SENDTIMEOUT));
    }
}
