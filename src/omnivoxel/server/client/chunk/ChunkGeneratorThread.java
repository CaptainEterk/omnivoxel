package omnivoxel.server.client.chunk;

import omnivoxel.server.ServerWorld;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class ChunkGeneratorThread implements Runnable {
    private static final AtomicReference<String> oldDate = new AtomicReference<>();
    private static final AtomicLong responseCount = new AtomicLong();
    private static final AtomicLong oldCount = new AtomicLong();

    private final ChunkGenerator chunkGenerator;
    private final BlockingQueue<ChunkTask> chunkTasks;
    private final ServerWorld world;
    private final Queue<ChunkTask> localQueue; // Reusable queue

    public ChunkGeneratorThread(ChunkGenerator chunkGenerator, BlockingQueue<ChunkTask> chunkTasks, ServerWorld world) {
        this.chunkGenerator = chunkGenerator;
        this.chunkTasks = chunkTasks;
        this.world = world;
        this.localQueue = new ArrayDeque<>();
    }

    @Override
    public void run() {
//        try {
//            while (!Thread.currentThread().isInterrupted()) {
//                int taskCount = chunkTasks.drainTo(localQueue);
//                if (taskCount > 0) {
//                    while (!localQueue.isEmpty()) {
//                        ChunkTask task = localQueue.remove();
//                        Position3D position3D = new Position3D(task.x(), task.y(), task.z());
//                        GeneratedChunk generatedChunk = chunkGenerator.generateChunk(position3D);
//                        ChunkResult chunkResult = GeneratedChunk.getResult(generatedChunk);
//                        sendChunkBytes(task.ctx(), task.x(), task.y(), task.z(), chunkResult.bytes());
//                        world.add(position3D, chunkResult.chunk());
//                    }
//                } else {
//                    Thread.sleep(1);
//                }
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
    }

    public BlockingQueue<ChunkTask> getChunkTasks() {
        return chunkTasks;
    }
}