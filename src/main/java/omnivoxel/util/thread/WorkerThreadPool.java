package omnivoxel.util.thread;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

public class WorkerThreadPool<T> {
    private final WorkerThread<T>[] workers;
    private volatile boolean running = true;

    @SuppressWarnings("unchecked")
    public WorkerThreadPool(int threadCount, Consumer<T> taskHandler) {
        this.workers = new WorkerThread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            WorkerThread<T> workerThread = new WorkerThread<>(new LinkedBlockingDeque<T>(), taskHandler);
            Thread thread = new Thread(workerThread, "Worker-" + i);
            workers[i] = workerThread;
            thread.start();
        }
    }

    public void submit(T task) throws InterruptedException {
        if (running) {
            BlockingQueue<T> smallestQueue = null;
            int smallestSize = Integer.MAX_VALUE;
            for (WorkerThread<T> workerThread : workers) {
                BlockingQueue<T> queue = workerThread.taskQueue();
                int size = queue.size();
                if (size < smallestSize) {
                    smallestQueue = queue;
                    smallestSize = size;
                    if (size == 0) {
                        break;
                    }
                }
            }
            if (smallestQueue != null) {
                smallestQueue.put(task);
            }
        }
    }

    public void shutdown() {
        running = false;
    }

    public void awaitTermination() {
        for (WorkerThread<T> worker : workers) {
            try {
                worker.stop();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public static final class WorkerThread<V> implements Runnable {
        private final BlockingQueue<V> taskQueue;
        private final Consumer<V> taskHandler;
        private final Queue<V> localQueue;

        public WorkerThread(BlockingQueue<V> taskQueue, Consumer<V> taskHandler) {
            this.taskQueue = taskQueue;
            this.taskHandler = taskHandler;
            this.localQueue = new ArrayDeque<>();
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    int taskCount = taskQueue.drainTo(localQueue);
                    if (taskCount > 0) {
                        while (!localQueue.isEmpty()) {
                            taskHandler.accept(localQueue.remove());
                        }
                    } else {
                        Thread.sleep(1);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void stop() throws InterruptedException {
            Thread.currentThread().join();
        }

        public BlockingQueue<V> taskQueue() {
            return taskQueue;
        }
    }
}