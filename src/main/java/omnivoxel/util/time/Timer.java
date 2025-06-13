package omnivoxel.util.time;

public class Timer {
    private final String timerName;
    private final long[] times;
    private int timeIndex = 0;

    private long startTime = 0;
    private boolean running = false;

    public Timer(String timerName, int timerSize) {
        this.timerName = timerName;
        times = new long[timerSize];
    }

    public void start() {
        if (running) {
            throw new IllegalStateException("Timer is already running.");
        }
        startTime = System.nanoTime();
        running = true;
    }

    public void stop() {
        if (!running) {
            throw new IllegalStateException("Timer is not running.");
        }
        long duration = System.nanoTime() - startTime;
        times[timeIndex++ % times.length] = duration;
        running = false;
    }

    public void printTimes() {
        System.out.println("Timer: " + timerName);
        for (int i = 0; i < timeIndex; i++) {
            System.out.printf("Run %d: %.3f ms%n", i + 1, times[i] / 1_000_000.0);
        }
    }

    public double averageTimes() {
        long total = 0;
        for (int i = timeIndex; i < timeIndex + times.length; i++) {
            total += times[i % times.length];
        }
        return (double) total / times.length;
    }

    public double time(Runnable runnable) {
        start();
        runnable.run();
        stop();
        return averageTimes();
    }
}