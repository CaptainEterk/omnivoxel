package omnivoxel.util.thread;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class WorkerThreadPool<T extends WorkerTask> {
    private final WorkerThread<T>[] workers;
    private final AtomicBoolean running;
    private final Set<T> pendingTasks;

    @SuppressWarnings("unchecked")
    public WorkerThreadPool(int threadCount, Supplier<BiFunction<T, Integer, Collection<T>>> taskHandlerSupplier, boolean daemon) {
        workers = new WorkerThread[threadCount];
        running = new AtomicBoolean(true);
        pendingTasks = ConcurrentHashMap.newKeySet();

        for (int i = 0; i < threadCount; i++) {
            WorkerThread<T> workerThread = new WorkerThread<>(new LinkedBlockingDeque<>(), taskHandlerSupplier.get(), running, pendingTasks);
            Thread thread = new Thread(workerThread, "Worker-" + i);
            thread.setDaemon(daemon);
            workers[i] = workerThread;
            thread.start();
        }
    }

    public void submit(T task) {
        submit(task, false);
    }

    public void submit(T task, boolean priority) {
        try {
            if (running.get() && task != null) {
                if (!pendingTasks.add(task)) {
                    task.reject();
                    return;
                }
                BlockingDeque<T> smallestQueue = null;
                int smallestSize = Integer.MAX_VALUE;
                for (WorkerThread<T> workerThread : workers) {
                    int size = workerThread.size();
                    if (size < smallestSize) {
                        smallestQueue = workerThread.taskQueue();
                        smallestSize = size;
                        if (size == 0) {
                            break;
                        }
                    }
                }
                if (smallestQueue != null) {
                    if (priority) {
                        smallestQueue.putFirst(task);
                    } else {
                        smallestQueue.put(task);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void shutdown() {
        running.set(false);
    }

    public void awaitTermination() {
        for (WorkerThread<T> worker : workers) {
            try {
                worker.thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean hasTask(T task) {
        return pendingTasks.contains(task);
    }

    public static final class WorkerThread<V extends WorkerTask> implements Runnable {
        private final BlockingDeque<V> taskQueue;
        private final BiFunction<V, Integer, Collection<V>> taskHandler;
        private final AtomicBoolean running;
        private final Set<V> pendingTasks;
        private Thread thread;

        public WorkerThread(BlockingDeque<V> taskQueue, BiFunction<V, Integer, Collection<V>> taskHandler, AtomicBoolean running, Set<V> pendingTasks) {
            this.taskQueue = taskQueue;
            this.taskHandler = taskHandler;
            this.running = running;
            this.pendingTasks = pendingTasks;
        }

        @Override
        public void run() {
            thread = Thread.currentThread();

            try {
                while (!Thread.currentThread().isInterrupted() && running.get()) {
                    V task = taskQueue.poll(100, TimeUnit.MILLISECONDS);

                    if (task != null) {
                        handleTask(task);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void handleTask(V task) {
            pendingTasks.remove(task);
            Collection<V> moreTasks = taskHandler.apply(task, size());
            if (moreTasks != null) {
                moreTasks.forEach(t -> {
                    if (pendingTasks.add(t)) {
                        taskQueue.add(t);
                    } else {
                        t.reject();
                    }
                });
            }
        }

        public int size() {
            return taskQueue.size();
        }

        public BlockingDeque<V> taskQueue() {
            return taskQueue;
        }
    }
}
