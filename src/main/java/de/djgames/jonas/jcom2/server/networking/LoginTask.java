package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.generated.ErrorType;
import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.generated.JComMessageType;
import de.djgames.jonas.jcom2.server.logging.Logger;
import de.djgames.jonas.jcom2.server.settings.Settings;

import java.text.Normalizer;
import java.util.UUID;
import java.util.concurrent.Callable;

import static de.djgames.jonas.jcom2.server.networking.Defaults.DEFAULT_UUID;

public class LoginTask implements Callable<Client> {

    private final Connection connection;
    private Client client;
    private UUID id;

    public LoginTask(Connection connection) {
        this.id = DEFAULT_UUID;
        this.connection = connection;
    }

    /**
     * Erlaubt nur ascii Charakter
     *
     * @param name Name des Clients
     * @return bereinigter Clientname
     */
    private String cleanUpName(String name) {
        String resultString = Normalizer.normalize(name, Normalizer.Form.NFKD);
        resultString = resultString.replaceAll("[^\\x00-\\x7F]", "");
        return resultString.substring(0, Math.min(Settings.MAX_NAME_LENGTH, resultString.length()));
    }

    @Override
    public Client call() {
        JComMessage loginMessage = this.connection.receiveMessage();
        int failCounter = 0;
        while (failCounter < Settings.LOGINTRIES) {
            if (loginMessage != null && loginMessage.getMessageType() == JComMessageType.LOGIN) {
                Logger.info("Player tries to log in");

                this.id = UUID.randomUUID();

                Logger.info("Spieler verbunden. ID=" + this.id);

                this.client = new Client(this.id, cleanUpName(loginMessage.getLogin().getName()), connection);

                //TODO stopWaitingForPlayers
            }
            //TODO implement reconnect? with a db of user accs maybe

            if (!this.id.equals(DEFAULT_UUID)) {
                connection.setId(this.id);
                this.connection.sendMessage(JComMessageFactory.createLoginReplyMessage(this.id), false);
                Logger.info("Login erfolgreich: " + client.getId() + " | " + client.getName());
                return this.client;
            }

            //Sende Fehlör
            this.connection.sendMessage(JComMessageFactory.createAcceptMessage(DEFAULT_UUID, ErrorType.AWAIT_LOGIN), true);

            failCounter++;
            // nach einem Fehler auf den naechsten Versuch warten
            loginMessage = this.connection.receiveMessage();
        }
        // Verlassen mit schwerem Fehlerfall
        // ID wird wieder freigegeben
        Logger.info("Client hat versagt sich einzuloggen: " + this.id);
        this.connection.disconnect(ErrorType.TOO_MANY_TRIES);
        return new Client(DEFAULT_UUID, "notLoggedIn", this.connection);
    }
}
