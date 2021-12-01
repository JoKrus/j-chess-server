package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.generated.MatchFormatData;
import net.jcom.jchess.server.generated.MatchTypeScore;
import net.jcom.jchess.server.generated.MatchTypeValue;

import java.util.concurrent.TimeUnit;

public class MatchDefaults {
    public static final MatchFormatData MATCH_FORMAT_DATA = new MatchFormatData() {{
        setMatchTypeValue(MatchTypeValue.SCORE);
        setMatchTypeData(new MatchTypeScore() {{
            this.setAmountToPlay(1);
            this.setPointsPerWin(2);
            this.setPointsPerDraw(1);
        }});
        setTimePerSide(TimeUnit.MINUTES.toSeconds(15));
        setTimePerSideIncrement(TimeUnit.SECONDS.toSeconds(10));
        setTimePerSidePerMove(TimeUnit.SECONDS.toSeconds(60));
    }};
}
