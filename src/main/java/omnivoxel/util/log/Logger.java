package omnivoxel.util.log;

import omnivoxel.common.settings.ConstantCommonSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class Logger {

    private static final int BUFFER_SIZE = 256;

    private static final ThreadLocal<LogBuffer> BUFFER =
            ThreadLocal.withInitial(LogBuffer::new);

    private static final StackWalker STACK_WALKER =
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private static volatile boolean showLogs = true;
    private static volatile Priority minPriority = Priority.LOW;

    private Logger() {
    }

    public static void setMinPriority(Priority priority) {
        minPriority = priority;
    }

    private static boolean allowed(Priority priority) {
        return priority.ordinal() >= minPriority.ordinal();
    }

    public static void setShowLogs(boolean showLogs) {
        Logger.showLogs = showLogs;
    }

    public static void error(String message) {
        logError(source(), Priority.NORMAL, message);
    }

    public static void warn(String message) {
        logWarn(source(), Priority.NORMAL, message);
    }

    public static void debug(String message) {
        logDebug(source(), Priority.LOW, message);
    }

    public static void info(String message) {
        logInfo(source(), Priority.NORMAL, message);
    }

    public static void error(Priority priority, String message) {
        logError(source(), priority, message);
    }

    public static void warn(Priority priority, String message) {
        logWarn(source(), priority, message);
    }

    public static void debug(Priority priority, String message) {
        logDebug(source(), priority, message);
    }

    public static void info(Priority priority, String message) {
        logInfo(source(), priority, message);
    }

    public static void error(String source, String message) {
        logError(source, Priority.NORMAL, message);
    }

    public static void warn(String source, String message) {
        logWarn(source, Priority.NORMAL, message);
    }

    public static void debug(String source, String message) {
        logDebug(source, Priority.LOW, message);
    }

    public static void info(String source, String message) {
        logInfo(source, Priority.NORMAL, message);
    }

    public static void error(String source, Priority priority, String message) {
        logError(source, priority, message);
    }

    public static void warn(String source, Priority priority, String message) {
        logWarn(source, priority, message);
    }

    public static void debug(String source, Priority priority, String message) {
        logDebug(source, priority, message);
    }

    public static void info(String source, Priority priority, String message) {
        logInfo(source, priority, message);
    }

    private static void logError(String source, Priority priority, String message) {
        if (!allowed(priority)) {
            return;
        }

        String formatted = format(priority, message);

        if (showLogs) {
            System.err.println("[" + source + "] " + formatted);
        }

        BUFFER.get().add(source, formatted);
    }

    private static void logWarn(String source, Priority priority, String message) {
        if (!allowed(priority)) {
            return;
        }

        String formatted = format(priority, message);

        if (showLogs) {
            System.err.println(
                    "\u001B[33m[" + source + "] " + formatted + "\u001B[0m"
            );
        }

        BUFFER.get().add(source, formatted);
    }

    private static void logDebug(String source, Priority priority, String message) {
        if (!allowed(priority)) {
            return;
        }

        String formatted = format(priority, message);

        if (showLogs) {
            System.out.println(
                    "\u001B[34m[" + source + "] " + formatted + "\u001B[0m"
            );
        }

        BUFFER.get().add(source, formatted);
    }

    private static void logInfo(String source, Priority priority, String message) {
        if (!allowed(priority)) {
            return;
        }

        String formatted = format(priority, message);

        if (showLogs) {
            System.out.println(
                    "\u001B[32m[" + source + "] " + formatted + "\u001B[0m"
            );
        }

        BUFFER.get().add(source, formatted);
    }

    private static String format(Priority priority, String message) {
        return "[" + priority + "] " + message;
    }

    private static String source() {
        return STACK_WALKER.walk(stream -> stream
                .map(StackWalker.StackFrame::getDeclaringClass)
                .filter(clazz -> clazz != Logger.class)
                .findFirst()
                .map(Class::getSimpleName)
                .orElse(Logger.class.getSimpleName()));
    }

    public static void writeRecentLogs() {
        BUFFER.get().write();
    }

    private static String sanitize(String source) {
        return source.toLowerCase().replace(' ', '_');
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH
    }

    private static final class LogBuffer {

        private final LogEntry[] entries = new LogEntry[BUFFER_SIZE];

        private int nextIndex;
        private int size;

        private void add(String source, String message) {
            entries[nextIndex] = new LogEntry(source, message);

            nextIndex = (nextIndex + 1) % BUFFER_SIZE;

            if (size < BUFFER_SIZE) {
                size++;
            }
        }

        private void write() {
            for (int i = 0; i < size; i++) {
                int index = (nextIndex - size + i + BUFFER_SIZE) % BUFFER_SIZE;
                LogEntry entry = entries[index];

                if (entry == null) {
                    continue;
                }

                writeFile(
                        Path.of(
                                ConstantCommonSettings.LOG_LOCATION
                                        + sanitize(entry.source)
                                        + ".log"
                        ),
                        entry.message
                );
            }
        }

        private static void writeFile(Path path, String message) {
            try {
                Path parent = path.getParent();

                if (parent != null) {
                    Files.createDirectories(parent);
                }

                Files.writeString(
                        path,
                        message + "\n",
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND,
                        StandardOpenOption.WRITE
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private record LogEntry(String source, String message) {
    }
}