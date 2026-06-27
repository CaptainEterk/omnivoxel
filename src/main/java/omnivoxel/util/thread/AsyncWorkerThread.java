package omnivoxel.util.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class AsyncWorkerThread<T> {
    private final Consumer<T> consumer;
    private final BlockingQueue<T> queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final int necessaryLimit;

    public AsyncWorkerThread(Consumer<T> consumer, boolean daemon, int necessaryLimit) {
        this.consumer = consumer;
        this.necessaryLimit = necessaryLimit;
        queue = new LinkedBlockingDeque<>();
        Thread thread = new Thread(this::run);
        thread.setDaemon(daemon);
        thread.start();
    }

    private void run() {
        try {
            while (!Thread.interrupted() && (queue.isEmpty() || running.get())) {
                T item = queue.poll(100L, TimeUnit.MILLISECONDS);
                if (item != null) {
                    consumer.accept(item);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean add(T task, boolean necessary) {
        if (necessary || queue.size() < necessaryLimit) {
            queue.add(task);
            return true;
        } else {
            return false;
        }
    }

    public void stop() {
        running.set(false);
    }

    public int size() {
        return queue.size();
    }
}