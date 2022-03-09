package net.jcom.jchess.server.data;

import net.jcom.jchess.server.generated.MatchStatusData;

public class MatchStatusDataExtended extends MatchStatusData {
    public String tournamentCode;

    public MatchStatusDataExtended(MatchStatusData matchStatusData, String tournamentCode) {
        setNamePlayer1(matchStatusData.getNamePlayer1());
        setNamePlayer2(matchStatusData.getNamePlayer2());
        setScorePlayer1(matchStatusData.getScorePlayer1());
        setScorePlayer2(matchStatusData.getScorePlayer2());
        this.tournamentCode = tournamentCode;
    }

    public String getTournamentCode() {
        return this.tournamentCode;
    }

    public void setTournamentCode(String tournamentCode) {
        this.tournamentCode = tournamentCode;
    }
}
