package omnivoxel.server.client.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.server.PackageID;
import omnivoxel.server.Position3D;
import omnivoxel.server.world.World;
import omnivoxel.server.world.chunk.result.ChunkResult;
import omnivoxel.server.world.chunk.result.GeneratedChunk;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkGeneratorThread implements Runnable {
    private final ChunkGenerator chunkGenerator;
    private final BlockingQueue<ChunkTask> chunkTasks;
    private final World world;
    private final Queue<ChunkTask> localQueue; // Reusable queue

    public ChunkGeneratorThread(ChunkGenerator chunkGenerator, World world) {
        this.chunkGenerator = chunkGenerator;
        this.world = world;
        this.chunkTasks = new LinkedBlockingQueue<>();
        this.localQueue = new ArrayDeque<>();
    }

    private void sendChunkBytes(ChannelHandlerContext ctx, int x, int y, int z, byte[] chunk) {
        ByteBuf buffer = Unpooled.buffer();
        int length = 16 + chunk.length;
        buffer.writeInt(length);
        buffer.writeInt(PackageID.CHUNK.ordinal());
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBytes(chunk);
        ctx.channel().writeAndFlush(buffer);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                int count = chunkTasks.drainTo(localQueue);
                if (count == 0) {
                    // Sleep briefly to avoid busy waiting
                    Thread.sleep(1);
                    continue;
                }

                for (ChunkTask task : localQueue) {
                    Position3D position3D = new Position3D(task.x(), task.y(), task.z());
                    GeneratedChunk generatedChunk = chunkGenerator.generateChunk(new ChunkPosition(task.x(), task.y(), task.z()));
                    ChunkResult chunkResult = GeneratedChunk.getResult(generatedChunk);
                    sendChunkBytes(task.ctx(), task.x(), task.y(), task.z(), chunkResult.bytes());
                    world.addChunk(position3D, chunkResult.chunk());
                }

                // Clear queue to reuse it
                localQueue.clear();
            }
        } catch (InterruptedException e) {
            // Exit gracefully on interruption
            Thread.currentThread().interrupt();
        }
    }

    public BlockingQueue<ChunkTask> getChunkTasks() {
        return chunkTasks;
    }
}