package net.jcom.jchess.server.logic;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler extends Timer {
    private final HashMap<Color, KickPlayer> players = new HashMap<>();

    public Scheduler() {
        super("Kick if move takes too long", true);
    }

    public void startTimer(Color color, Runnable runnable) {
        this.players.put(color, new Scheduler.KickPlayer(runnable));
        this.schedule(this.players.get(color), MatchDefaults.MATCH_FORMAT_DATA.getTimePerSidePerMove());
    }

    public void stopTimer(Color color) {
        Scheduler.KickPlayer player = this.players.get(color);
        if (player != null) {
            player.cancel();
        }

    }

    public static class KickPlayer extends TimerTask {
        Runnable runnable;

        public KickPlayer(Runnable runnable) {
            this.runnable = runnable;
        }

        public void run() {
            this.runnable.run();
        }
    }
}