package de.djgames.jonas.jcom2.server.logic;

import de.djgames.jonas.jcom2.server.Server;
import de.djgames.jonas.jcom2.server.factory.JComMessageFactory;
import de.djgames.jonas.jcom2.server.generated.*;
import de.djgames.jonas.jcom2.server.logic.map.Coordinate;
import de.djgames.jonas.jcom2.server.logic.map.GameMap;
import de.djgames.jonas.jcom2.server.logic.unit.LogicHelpers;
import de.djgames.jonas.jcom2.server.networking.Player;
import de.djgames.jonas.jcom2.server.networking.PlayerStatus;
import de.djgames.jonas.jcom2.server.settings.Settings;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static de.djgames.jonas.jcom2.server.StartServer.logger;

public class Match {
    private final List<Player> playerList;
    private final UUID matchId;
    private final Random randomStart;
    private GameMap gameMap;
    private Player winner;
    private final PointData pointData;
    private WinningReason winningReason = WinningReason.UNDEFINED;
    private static final List<Team> teamList = List.of(Team.BLUE, Team.RED);

    public Match(List<Player> playerList, UUID matchId) {
        this.playerList = playerList;
        this.matchId = matchId;
        this.randomStart = new Random();
        this.pointData = new PointData();
    }

    public void startMatch() {
        try {
            if (!matchInit()) {
                logger.error("One player could not initialize successfully");
                matchTearDown();
                return;
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            matchTearDown();
            return;
        }
        try {
            if (!matchLoop()) {
                logger.error("One player sent something invalid during playing");
                matchTearDown();
                return;
            }
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

    private boolean matchInit() {
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

        this.gameMap = initMap();

        logger.info("Match " + this.matchId + ": Send begin.");

        //Init team, send Begin and receive Position
        var beginnerName = this.playerList.get(0).getPlayerName();
        for (int i = 0; i < this.playerList.size(); i++) {
            Player player = this.playerList.get(i);
            Team team = teamList.get(i);
            Map<UUID, Boolean> soldierIds =
                    List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()).stream().
                            collect(Collectors.toMap(uuid -> uuid, uuid -> false));
            //BEGIN message mit namen von startSpieler und map
            player.getCommunicator().sendMessageOrRemove(
                    JComMessageFactory.createBeginMessage(player.getId(), this.gameMap.toGameMapData(),
                            beginnerName, team, Settings.getInt(Settings.SCORE_GOAL), new ArrayList<>(soldierIds.keySet())));
            var posSoldPair = LogicHelpers.getMatchMessage(player, PositionSoldiersMessage.class,
                    ErrorType.AWAIT_POSITION_SOLDIERS);

            if (checkMessageNotNull(i, posSoldPair.getLeft())) {
                return false;
            }

            //TODO validate before and otherwise send IllegalMove and try to read again if error < MAX
            for (var posData : posSoldPair.getLeft().getPositions()) {
                var uuid = UUID.fromString(posData.getSoldierId());
                Boolean alreadyAdded = soldierIds.get(uuid);
                if (alreadyAdded == null /*not in it*/ || alreadyAdded) {
                    checkMessageNotNull(i, null);
                    return false;
                } else {
                    soldierIds.put(uuid, true);
                }

                this.gameMap.spawnSoldier(team, new Coordinate(posData.getPosition()), uuid);
            }
        }
        return true;
    }

    private boolean matchLoop() {
        logger.info("Match " + this.matchId + ": starts now.");
        boolean matchOngoing = true;

        int currentPlayerIdx = 0;
        while (matchOngoing) {
            Player currPlayer = this.playerList.get(currentPlayerIdx);
            Team currTeam = teamList.get(currentPlayerIdx);
            Team enemyTeam = teamList.get(currentPlayerIdx + 1 % 2);

            var soldiersOfTeam = this.gameMap.getSoldiersOfTeam(currTeam);

            var soldiersOfEnemyTeam = this.gameMap.getSoldiersOfTeam(enemyTeam);
            //TODO Filter vision


            List<PositionSoldierData> positionSoldierDataList = getPositionSoldierData(soldiersOfTeam);

            List<PositionSoldierData> enemyPositionSoldierDataList = getPositionSoldierData(soldiersOfEnemyTeam);

            var ahcd = new AllHitChanceData();

            currPlayer.getCommunicator().sendMessage(JComMessageFactory.createYourTurnMessage(currPlayer.getId(),
                    new ArrayList<>(soldiersOfTeam.keySet()),
                    positionSoldierDataList,
                    new ArrayList<>(soldiersOfEnemyTeam.keySet()),
                    enemyPositionSoldierDataList,
                    this.pointData,
                    this.gameMap.toGameMapData(),
                    ahcd
            ));

            //TODO, receive msg Action or FT until ft
            matchOngoing = false;
        }

        //TODO replace winner
        this.winner = this.playerList.get(0);
        return true;
    }

    private List<PositionSoldierData> getPositionSoldierData(Map<UnitData, PositionData> soldiersOfTeam) {
        var positionSoldierDataList = soldiersOfTeam.keySet().stream().map(unit -> {
            PositionSoldierData positionSoldierData = new PositionSoldierData();
            positionSoldierData.setPosition(soldiersOfTeam.get(unit));
            positionSoldierData.setSoldierId(unit.getId());
            return positionSoldierData;
        }).collect(Collectors.toList());
        return positionSoldierDataList;
    }

    private void matchTearDown() {
        for (var player : this.playerList) {
            player.setStatus(PlayerStatus.QUEUE);
            var msg = JComMessageFactory.createGameOverMessage(player.getId(),
                    this.winner == null ? "" : this.winner.getPlayerName(),
                    "winningReason:" + this.winningReason.name());
            player.getCommunicator().sendMessage(msg);
        }
        logger.info("Match " + this.matchId + ": is over.");
        //TODO maybe save game to sqlite or something
    }

    private boolean checkMessageNotNull(int playerId, Object message) {
        if (message == null) {
            this.winner = this.playerList.get((playerId + 1) % 2);
            this.winningReason = WinningReason.NO_VALID_MOVE_IN_TIME;
            return true;
        }
        return false;
    }

    private GameMap initMap() {
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
        return new GameMap(mapString);
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
