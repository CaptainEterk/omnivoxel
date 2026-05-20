package omnivoxel.server.io;

import omnivoxel.util.thread.AsyncWorkerThread;

public class CacheIO {
    private static final AsyncWorkerThread<CacheItem> cacheAsyncWorkerThread = new AsyncWorkerThread<>(CacheHandler::cache, false);

    public static void add(CacheItem cacheItem) {
        cacheAsyncWorkerThread.add(cacheItem);
    }

    public static void stop() {
        cacheAsyncWorkerThread.stop();
    }
}