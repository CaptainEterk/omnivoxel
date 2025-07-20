package omnivoxel.util.log;

import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.util.time.Timer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Logger {
    private final String logName;
    private final Queue<String> logs;
    private final Queue<String> debugLogs;
    private final Map<String, Timer> timers;

    public Logger(String logName) {
        this.logName = logName;
        this.logs = new ConcurrentLinkedDeque<>();
        this.debugLogs = new ConcurrentLinkedDeque<>();
        timers = new HashMap<>();
    }

    public void error(String error) {
        logs.add(error);
        debugLogs.add(error);
        try {
            writeInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void warn(String warn) {
        debugLogs.add(warn);
        try {
            write();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void debug(String debug) {
        debugLogs.add(debug);
        try {
            write();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void info(String info) {
        logs.add(info);
        debugLogs.add(info);
        try {
            writeInfo();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Timer getTimer(String name) {
        return timers.get(name);
    }

    public void addTimer(String name, Timer timer) {
        timers.put(name, timer);
    }

    public void writeDebug() throws IOException {
        Path debugPath = Path.of(ConstantGameSettings.LOG_LOCATION + logName + "_debug.log");
        Files.write(debugPath, String.join("\n", debugLogs).getBytes());
    }

    public void writeInfo() throws IOException {
        Path infoPath = Path.of(ConstantGameSettings.LOG_LOCATION + logName + ".log");
        Files.write(infoPath, String.join("\n", logs).getBytes());
    }

    public void write() throws IOException {
        writeDebug();
        writeInfo();
    }
}