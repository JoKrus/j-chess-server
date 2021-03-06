package net.jcom.jchess.server.settings;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.prefs.Preferences;

import static net.jcom.jchess.server.StartServer.logger;

public class Settings {
    private static final Preferences PREFERENCES =
            Preferences.userRoot().node(Settings.class.getCanonicalName());
    public static final String LOGIN_TRIES = "LOGIN_TRIES";
    public static final int LOGIN_TRIES_DEFAULT = 3;
    public static final String PORT = "PORT";
    public static final int PORT_DEFAULT = 5123;
    public static final String SSL_PORT = "SSL_PORT";
    public static final int SSL_PORT_DEFAULT = 5124;
    public static final String MAX_NAME_LENGTH = "MAX_NAME_LENGTH";
    public static final int MAX_NAME_LENGTH_DEFAULT = 30;

    static {
        initDefault(LOGIN_TRIES, LOGIN_TRIES_DEFAULT);
        initDefault(PORT, PORT_DEFAULT);
        initDefault(SSL_PORT, SSL_PORT_DEFAULT);
        initDefault(MAX_NAME_LENGTH, MAX_NAME_LENGTH_DEFAULT);
    }

    private static void initDefault(String keyName, int keyDefault) {
        if (PREFERENCES.getInt(keyName, keyDefault) == keyDefault)
            PREFERENCES.putInt(keyName, keyDefault);
    }

    private static void initDefault(String keyName, String keyDefault) {
        if (PREFERENCES.get(keyName, keyDefault).equals(keyDefault))
            PREFERENCES.put(keyName, keyDefault);
    }

    public static int getInt(String key) {
        try {
            Field defaultField = Settings.class.getDeclaredField(key + "_DEFAULT");
            return Settings.PREFERENCES.getInt(key, defaultField.getInt(null));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return 1;
    }

    public static void load(String path) {
        if (path == null) return;
        Properties prop = new Properties();
        try (var fis = FileUtils.openInputStream(new File(path))) {
            prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadIntPreferenceFromProperty(prop, LOGIN_TRIES);
        loadIntPreferenceFromProperty(prop, PORT);
        loadIntPreferenceFromProperty(prop, SSL_PORT);
        loadIntPreferenceFromProperty(prop, MAX_NAME_LENGTH);
    }

    private static void loadIntPreferenceFromProperty(Properties prop, String key) {
        var valueString = prop.getProperty(key);
        if (valueString != null) PREFERENCES.putInt(key, Integer.parseInt(valueString));
    }

    private static void loadStringPreferenceFromProperty(Properties prop, String key) {
        var valueString = prop.getProperty(key);
        if (valueString != null) PREFERENCES.put(key, valueString);
    }
}
