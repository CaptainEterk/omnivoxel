package omnivoxel.util.thread;

import java.util.function.Consumer;

public final class CoalescingWorkerThread<T> {
    private final Object lock = new Object();

    private volatile boolean running = true;

    private T pendingInput;
    private T currentInput;

    private final Thread worker;

    public CoalescingWorkerThread(Consumer<T> consumer, boolean daemon) {
        worker = new Thread(() -> {
            while (running) {

                // Wait until there is work.
                synchronized (lock) {
                    while (running && pendingInput == null) {
                        try {
                            lock.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }

                    if (!running) {
                        break;
                    }

                    // Atomically swap pending -> current.
                    currentInput = pendingInput;
                    pendingInput = null;
                }

                try {
                    consumer.accept(currentInput);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Replaces any pending work with the newest input.
     */
    public void updateInput(T input) {
        synchronized (lock) {
            pendingInput = input;
            lock.notify();
        }
    }

    public T getCurrentInput() {
        return currentInput;
    }

    public void shutdown() {
        synchronized (lock) {
            running = false;
            lock.notifyAll();
        }
    }
}