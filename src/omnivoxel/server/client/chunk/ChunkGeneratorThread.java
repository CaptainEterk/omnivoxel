package omnivoxel.server.client.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.server.PackageID;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkGeneratorThread implements Runnable {
    private final ChunkGenerator chunkGenerator;
    private final BlockingQueue<ChunkTask> chunkTasks;
    private final Map<ChunkPosition, byte[]> generatedChunks;

    public ChunkGeneratorThread(ChunkGenerator chunkGenerator, Map<ChunkPosition, byte[]> generatedChunks) {
        this.chunkGenerator = chunkGenerator;
        this.generatedChunks = generatedChunks;
        this.chunkTasks = new LinkedBlockingQueue<>();
    }

    private byte[] sendChunkBytes(ChannelHandlerContext ctx, PackageID id, int x, int y, int z, byte[] chunk) {
        ByteBuf buffer = Unpooled.buffer();
        int length = 16 + chunk.length;
        buffer.writeInt(length);
        buffer.writeInt(id.ordinal());
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeBytes(chunk);
        ctx.channel().writeAndFlush(buffer);
        return chunk;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ChunkTask c = chunkTasks.take();
                ChunkPosition chunkPosition = new ChunkPosition(c.x(), c.y(), c.z());
                byte[] chunk = generatedChunks.get(chunkPosition);
                if (chunk == null) {
                    generatedChunks.put(chunkPosition, generateChunk(c));
                } else {
                    sendChunkBytes(c.ctx(), PackageID.CHUNK_RESPONSE, c.x(), c.y(), c.z(), chunk);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generateChunk(ChunkTask chunkTask) {
        byte[] chunk = chunkGenerator.generateChunk(new ChunkPosition(chunkTask.x(), chunkTask.y(), chunkTask.z()));
        return sendChunkBytes(chunkTask.ctx(), PackageID.CHUNK_RESPONSE, chunkTask.x(), chunkTask.y(), chunkTask.z(), chunk);
    }

    public BlockingQueue<ChunkTask> getChunkTasks() {
        return chunkTasks;
    }
}