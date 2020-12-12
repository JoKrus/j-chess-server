package de.djgames.jonas.jcom2.server.logic;

import de.djgames.jonas.jcom2.server.Server;
import de.djgames.jonas.jcom2.server.factory.JComMessageFactory;
import de.djgames.jonas.jcom2.server.generated.ErrorType;
import de.djgames.jonas.jcom2.server.generated.PositionSoldiersMessage;
import de.djgames.jonas.jcom2.server.generated.Team;
import de.djgames.jonas.jcom2.server.logic.map.Coordinate;
import de.djgames.jonas.jcom2.server.logic.map.GameMap;
import de.djgames.jonas.jcom2.server.logic.unit.LogicHelpers;
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
    private Player winner;

    public Match(List<Player> playerList, UUID matchId) {
        this.playerList = playerList;
        this.matchId = matchId;
        this.randomStart = new Random();
    }

    public void startMatch() {
        try {
            matchInit();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            matchTearDown();
            return;
        }
        try {
            matchLoop();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            matchTearDown();
            return;
        }
        try {
            matchTearDown();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    private void matchInit() {
        logger.info("Match " + this.matchId + ": Send found.");
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

        logger.info("Match " + this.matchId + ": Load map.");

        var mapString = "";
        try {
            var loader = Server.class.getClassLoader();
            InputStream is = loader.getResourceAsStream("maps/testmap.jmap");
            if (is == null) {
                throw new IOException("Could not parse map path");
            }
            mapString = IOUtils.toString(is, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Could not load map", e);
        }
        this.gameMap = new GameMap(mapString);

        logger.info("Match " + this.matchId + ": Send begin.");

        //Init team, send Begin and receive Position
        var teamList = List.of(Team.BLUE, Team.RED);
        var beginnerName = this.playerList.get(0).getPlayerName();
        for (int i = 0; i < this.playerList.size(); i++) {
            Player player = this.playerList.get(i);
            Team team = teamList.get(i);
            //BEGIN message mit namen von startSpieler und map
            player.getCommunicator().sendMessageOrRemove(
                    JComMessageFactory.createBeginMessage(player.getId(), this.gameMap.toGameMapData(),
                            beginnerName, team));

            var posSold = LogicHelpers.getMatchMessage(player, PositionSoldiersMessage.class);

            for (var posData : posSold.getPositions()) {
                this.gameMap.spawnSoldier(team, new Coordinate(posData));
            }
        }
    }

    private void matchLoop() {
        logger.info("Match " + this.matchId + ": starts now.");
        boolean matchOngoing = true;
        //TODO remove
        int counter = 0;
        while (matchOngoing) {
            for (var player : this.playerList) {
                player.getCommunicator().sendMessage(JComMessageFactory.createAcceptMessage(player.getId(),
                        ErrorType.NO_ERROR));
                var answer = player.getCommunicator().receiveMessage();
            }
            counter++;
            if (counter > 3) {
                matchOngoing = false;
            }
        }

        this.winner = this.playerList.get(0);
    }


    private void matchTearDown() {
        for (var player : this.playerList) {
            player.setStatus(PlayerStatus.QUEUE);
            var msg = JComMessageFactory.createGameOverMessage(player.getId(),
                    this.winner == null ? "" : this.winner.getPlayerName(), "");
            player.getCommunicator().sendMessage(msg);
        }
        logger.info("Match " + this.matchId + ": is over.");
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
