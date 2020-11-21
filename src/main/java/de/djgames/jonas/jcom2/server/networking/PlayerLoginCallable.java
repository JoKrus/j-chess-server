package de.djgames.jonas.jcom2.server.networking;

import de.djgames.jonas.jcom2.server.Server;
import de.djgames.jonas.jcom2.server.factory.JComMessageFactory;
import de.djgames.jonas.jcom2.server.generated.ErrorType;
import de.djgames.jonas.jcom2.server.generated.JComMessage;
import de.djgames.jonas.jcom2.server.generated.JComMessageType;
import de.djgames.jonas.jcom2.server.generated.LoginMessage;
import de.djgames.jonas.jcom2.server.settings.Settings;

import java.text.Normalizer;
import java.util.UUID;
import java.util.concurrent.Callable;

import static de.djgames.jonas.jcom2.server.networking.Defaults.DEFAULT_UUID;

public class PlayerLoginCallable implements Callable<Player> {

    private final Communicator communicator;

    public PlayerLoginCallable(Communicator communicator) {
        this.communicator = communicator;
    }

    private String cleanUpName(String name) {
        String resultString = Normalizer.normalize(name, Normalizer.Form.NFKD);
        resultString = resultString.replaceAll("[^\\x00-\\x7F]", "");
        return resultString.substring(0, Math.min(Settings.getInt(Settings.MAX_NAME_LENGTH), resultString.length()));
    }

    @Override
    public Player call() {
        //Login happens here
        int failedLoginCounter = 0;
        Player player = null;
        do {
            JComMessage message = this.communicator.receiveMessage();
            if (message != null && message.getMessageType() == JComMessageType.LOGIN && message.getLogin() != null) {
                LoginMessage loginMessage = message.getLogin();
                String name = cleanUpName(loginMessage.getName());
                UUID id = UUID.randomUUID();

                if (Server.getInstance().getConnectedPlayerNames().contains(name)) {
                    this.communicator.sendMessage(JComMessageFactory.createAcceptMessage(id, ErrorType.DUPLICATE_NAME));
                    failedLoginCounter++;
                    continue;
                }
                player = new Player(id, this.communicator, name);
            }

            if (player != null && !DEFAULT_UUID.equals(player.getId())) {
                this.communicator.sendMessage(JComMessageFactory.createLoginReplyMessage(player.getId()));
                return player;
            }

            this.communicator.sendMessage(JComMessageFactory.createAcceptMessage(DEFAULT_UUID, ErrorType.AWAIT_LOGIN));
            failedLoginCounter++;
        } while (failedLoginCounter < Settings.getInt(Settings.LOGIN_TRIES));

        this.communicator.sendMessage(JComMessageFactory.createAcceptMessage(DEFAULT_UUID, ErrorType.TOO_MANY_TRIES));
        this.communicator.close();
        return player;
    }
}
