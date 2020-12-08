package de.djgames.jonas.jcom2.server.logic;

import de.djgames.jonas.jcom2.server.factory.JComMessageFactory;
import de.djgames.jonas.jcom2.server.generated.ErrorType;
import de.djgames.jonas.jcom2.server.logic.map.GameMap;
import de.djgames.jonas.jcom2.server.networking.Player;
import de.djgames.jonas.jcom2.server.networking.PlayerStatus;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static de.djgames.jonas.jcom2.server.StartServer.logger;

public class Match {
    private final List<Player> playerList;
    private final UUID matchId;
    private final Random randomStart;
    private GameMap gameMap;

    public Match(List<Player> playerList, UUID matchId) {
        this.playerList = playerList;
        this.matchId = matchId;
        randomStart = new Random();
    }

    public void startMatch() {
        for (var player : this.playerList) {
            player.getCommunicator().sendMessageOrRemove(
                    JComMessageFactory.createGameFoundMessage(player.getId(), this.matchId,
                            //get other player
                            this.playerList.stream().filter(player1 ->
                                    !player1.equals(player)).findFirst().get().getPlayerName()));
            player.setStatus(PlayerStatus.IN_GAME);
        }
        //index 0 is startPlayer
        Collections.shuffle(this.playerList, this.randomStart);

        var mapString = "";
        try {
            var currT = Thread.currentThread();
            var loader = currT.getContextClassLoader();
            InputStream is = loader.getResourceAsStream("maps/default.jmap");
            mapString = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Could not load map", e);
        }
        this.gameMap = new GameMap(mapString);
        // logger.info("Map loaded:" + System.lineSeparator() + mapString);

        //BEGIN message mit namen von startSpieler und map


        matchLogic();
    }

    private void matchLogic() {


        boolean matchOngoing = true;
        //TODO remove
        int counter = 0;
        while (matchOngoing) {
            for (var player : this.playerList) {
                player.getCommunicator().sendMessage(JComMessageFactory.createAcceptMessage(player.getId(),
                        ErrorType.NO_ERROR));
                var answer = player.getCommunicator().receiveMessage();

                logger.debug(answer.getMessageType() + " received by " + player.getPlayerName());
            }
            counter++;
            if (counter > 3) {
                matchOngoing = false;
            }
        }
        for (var player : this.playerList) {
            var msg = JComMessageFactory.createGameOverMessage(player.getId(),
                    this.playerList.get(0).getPlayerName(), "");
            player.getCommunicator().sendMessage(msg);
            player.setStatus(PlayerStatus.QUEUE);
        }
        //TODO maybe save game to sqlite or something
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(this.matchId, match.matchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.matchId);
    }
}
