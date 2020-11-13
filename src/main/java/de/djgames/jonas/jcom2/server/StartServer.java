package de.djgames.jonas.jcom2.server;

import de.djgames.jonas.jcom2.server.logging.Logger;
import de.djgames.jonas.jcom2.server.logging.LoggerBuilder;
import de.djgames.jonas.jcom2.server.settings.Settings;
import org.apache.commons.cli.*;

import java.text.SimpleDateFormat;

public class StartServer {
    static String configPath = null;
    public static Logger logger = LoggerBuilder.init().format(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"))
            .addWriteStream(System.out, System.err).minimumLevel(Logger.LoggingLevel.DEBUG).build();

    public static void main(String[] args) {
        parseArgs(args);
        // Wenn mit null aufgerufen, werden Standardwerte benutzt
        Settings.load(configPath);
        Server.getInstance().waitForConnections();
    }

    public static void parseArgs(String[] args) {
        Options availableOptions = new Options();
        availableOptions.addOption(Option.builder("c").
                hasArg(true).argName("CONFIG_PATH").
                desc("Path to property file for configuration").build());
        availableOptions.addOption(Option.builder("h").
                hasArg(false).
                desc("Displays this help message").build());
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(availableOptions, args);
            if (cmd.hasOption("h")) {
                printCMDHelp(0, availableOptions);
                //exits Program
            }
            var config = cmd.getOptionValue("c");
            if (config != null) {
                configPath = config;
            }
        } catch (ParseException e) {
            printCMDHelp(1, availableOptions);
        }
    }

    private static void printCMDHelp(int exitCode, Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar jComServer.jar [options]\nAvailable Options:", options);
        System.exit(exitCode);
    }
}
