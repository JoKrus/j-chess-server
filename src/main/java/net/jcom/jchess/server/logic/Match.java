package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.factory.JChessMessageFactory;
import net.jcom.jchess.server.generated.MatchFormatData;
import net.jcom.jchess.server.generated.MatchStatusData;
import net.jcom.jchess.server.generated.MatchTypeScore;
import net.jcom.jchess.server.generated.MatchTypeValue;
import net.jcom.jchess.server.networking.Player;
import net.jcom.jchess.server.networking.PlayerStatus;

import java.util.*;

import static net.jcom.jchess.server.StartServer.logger;

public class Match {
    private final List<Player> playerList;
    private final UUID matchId;
    private final Random randomStart;
    private final MatchFormatData matchFormatData = MatchDefaults.MATCH_FORMAT_DATA;
    private final MatchStatusData matchStatusData = new MatchStatusData();

    public Match(List<Player> playerList, UUID matchId) {
        this.playerList = playerList;
        this.matchId = matchId;
        this.randomStart = new Random();
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
                    JChessMessageFactory.createMatchFoundMessage(player.getId(), this.matchId,
                            this.matchFormatData,
                            //get other player
                            this.playerList.stream().filter(player1 ->
                                    !player1.equals(player)).findFirst().get().getPlayerName()));
            player.setStatus(PlayerStatus.IN_GAME);
        }

        //index 0 is startPlayer
        Collections.shuffle(this.playerList, this.randomStart);

        return true;
    }

    private boolean matchLoop() {
        logger.info("Match " + this.matchId + ": starts now.");

        this.matchStatusData.setNamePlayer1(this.playerList.get(0).getPlayerName());
        this.matchStatusData.setNamePlayer2(this.playerList.get(1).getPlayerName());

        if (this.matchFormatData.getMatchTypeValue() == MatchTypeValue.SCORE) {
            return matchLoopScore();
        } else if (this.matchFormatData.getMatchTypeValue() == MatchTypeValue.WIN_X) {
            return matchLoopWinX();
        } else {
            return false;
        }
    }

    private boolean matchLoopWinX() {
        return false;
    }

    private boolean matchLoopScore() {
        MatchTypeScore scoreData = (MatchTypeScore) this.matchFormatData.getMatchTypeData();

        for (int i = 0; i < scoreData.getAmountToPlay(); i++) {
            Collections.reverse(this.playerList);
            Game game = new Game(this.playerList);//playerList
            game.start();
            var result = game.getResult();

            boolean isPlayer1White = this.playerList.get(0).getPlayerName().equals(this.matchStatusData.getNamePlayer1());
            //get 0 is white

            switch (result) {
                case BLACK:
                    if (isPlayer1White) {
                        this.matchStatusData.setScorePlayer2(this.matchStatusData.getScorePlayer2() + scoreData.getPointsPerWin());
                    } else {
                        this.matchStatusData.setScorePlayer1(this.matchStatusData.getScorePlayer1() + scoreData.getPointsPerWin());
                    }
                    break;
                case WHITE:
                    if (!isPlayer1White) {
                        this.matchStatusData.setScorePlayer2(this.matchStatusData.getScorePlayer2() + scoreData.getPointsPerWin());
                    } else {
                        this.matchStatusData.setScorePlayer1(this.matchStatusData.getScorePlayer1() + scoreData.getPointsPerWin());
                    }
                    break;
                case DRAW:
                    this.matchStatusData.setScorePlayer1(this.matchStatusData.getScorePlayer1() + scoreData.getPointsPerDraw());
                    this.matchStatusData.setScorePlayer2(this.matchStatusData.getScorePlayer2() + scoreData.getPointsPerDraw());
                    break;
            }

            for (var player : this.playerList) {
                player.getCommunicator().sendMessage(JChessMessageFactory.createMatchStatusMessage(player.getId(), this.matchStatusData));
            }
        }
        return true;
    }

    private void matchTearDown() {
        for (var player : this.playerList) {
            player.setStatus(PlayerStatus.QUEUE);
            var msg = JChessMessageFactory.createMatchOverMessage(player.getId(),
                    this.matchFormatData, this.matchStatusData);
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
