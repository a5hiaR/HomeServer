import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Logger {
    private static final Path LOG_FILE_PATH = Paths.get("json.log");
    private static final Path TEMP_LOG_FILE_PATH = Paths.get("json.log.tmp");
    private static final BlockingQueue<JSONObject> logBuffer = new LinkedBlockingQueue<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private String tag;

    static {
        // Schedule the flush operation to run periodically from a background thread.
        scheduler.scheduleAtFixedRate(Logger::flushBuffer, 5, 5, TimeUnit.SECONDS);
    }

    public Logger(String tag) {
        this.tag = tag;
    }

    public void log(String level, HashMap<String, String> data) {
        JSONObject entry = new JSONObject();

        entry.put("level", level);
        entry.put("tag", this.tag);
        entry.put("timestamp", Instant.now().toString());
        
        data.forEach((k, v) -> entry.put(k, v));

        System.out.println(entry.toString());

        logBuffer.add(entry);
    }

    public static void shutdown() {
        flushBuffer(); // Final flush before shutdown
        scheduler.shutdown();
    }

    private static void flushBuffer() {
        if (!logBuffer.isEmpty()) {

            List<JSONObject> newLogs = new ArrayList<>();
            logBuffer.drainTo(newLogs);
            String oldLogs = "";
            
            try {
                if (Files.exists(LOG_FILE_PATH)) {
                    oldLogs = Files.readString(LOG_FILE_PATH);
                }

                StringBuilder log = new StringBuilder(oldLogs);

                for(JSONObject entry : newLogs) {
                    log.append(entry.toString());
                    log.append(System.lineSeparator());
                }

                byte[] logBytes = log.toString().getBytes();
                Files.write(TEMP_LOG_FILE_PATH, logBytes);
                Files.move(TEMP_LOG_FILE_PATH, LOG_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                System.err.println(exception + "@Logger flushBuffer()");
            }
        }
    }
}