package net.jcom.jchess.server.fileio;

import com.google.gson.Gson;
import net.jcom.jchess.server.data.MatchStatusDataExtended;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SuperFileHandler24 {
    public static final Gson GSON = new Gson();
    public static final String NOT_SET = "amkichbineinhuhnundhabenixzutun";
    public static final String JSON_END = ".json";
    private static final SuperFileHandler24 instance = new SuperFileHandler24();
    private static final AtomicBoolean running = new AtomicBoolean(true);
    public static String STORAGE_FOLDER = NOT_SET;
    private final ConcurrentLinkedQueue<RequestHandle> requestQueue;

    public SuperFileHandler24() {
        this.requestQueue = new ConcurrentLinkedQueue<>();

        Thread runner = new Thread(this::process);
        runner.setDaemon(true);
        runner.start();
    }

    public static SuperFileHandler24 getInstance() {
        return instance;
    }

    public void registerMatchResult(MatchStatusDataExtended matchStatusData, Callback callback) {
        this.requestQueue.add(new RequestHandle(matchStatusData, callback));
    }

    private void process() {
        RequestHandle requestHandle;
        while (running.get()) {
            if (this.requestQueue.isEmpty()) continue;
            if (STORAGE_FOLDER.equals(NOT_SET)) continue;
            requestHandle = this.requestQueue.poll();
            try {
                Path path = Paths.get(STORAGE_FOLDER,
                        requestHandle.matchStatusData.getTournamentCode() + JSON_END);
                FileUtils.deleteQuietly(path.toFile());
                FileUtils.writeStringToFile(path.toFile(),
                        GSON.toJson(requestHandle.matchStatusData), StandardCharsets.UTF_8);
                requestHandle.callback.execute(Status.SUCCESS);
            } catch (IOException e) {
                requestHandle.callback.execute(Status.FAILED);
            }
        }
    }

    public enum Status {
        SUCCESS, FAILED
    }

    @FunctionalInterface
    public interface Callback {
        void execute(Status status);
    }

    private static class RequestHandle {
        private final MatchStatusDataExtended matchStatusData;
        private final Callback callback;

        private RequestHandle(MatchStatusDataExtended matchStatusData, Callback callback) {
            this.matchStatusData = matchStatusData;
            this.callback = callback;
        }
    }
}
