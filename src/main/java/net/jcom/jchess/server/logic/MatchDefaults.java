package net.jcom.jchess.server.logic;

import net.jcom.jchess.server.generated.MatchFormatData;
import net.jcom.jchess.server.generated.MatchTypeScore;
import net.jcom.jchess.server.generated.MatchTypeValue;

import java.util.concurrent.TimeUnit;

public class MatchDefaults {
    public static final MatchFormatData MATCH_FORMAT_DATA = new MatchFormatData() {{
        setMatchTypeValue(MatchTypeValue.SCORE);
        setMatchTypeData(new MatchTypeScore() {{
            this.setAmountToPlay(4);
            this.setPointsPerWin(2);
            this.setPointsPerDraw(1);
        }});
        setTimePerSide(TimeUnit.MINUTES.toMillis(15));
        setTimePerSideIncrement(TimeUnit.SECONDS.toMillis(10));
        setTimePerSidePerMove(TimeUnit.SECONDS.toMillis(60));
    }};
}
