package omnivoxel.server.client.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.server.PackageID;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkGeneratorThread implements Runnable {
    private final ChunkGenerator chunkGenerator;
    private final BlockingQueue<ChunkTask> chunkTasks;

    public ChunkGeneratorThread(ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
        this.chunkTasks = new LinkedBlockingQueue<>();
    }

    private void sendChunkBytes(ChannelHandlerContext ctx, PackageID id, int x, int y, int z, byte[] chunk) {
        ByteBuf buffer = Unpooled.buffer();
        int length = 16 + chunk.length;
        buffer.writeInt(length);
        buffer.writeInt(id.ordinal());
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
                ChunkTask c = chunkTasks.take();
                generateChunk(c);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateChunk(ChunkTask chunkTask) {
        byte[] chunk = chunkGenerator.generateChunk(new ChunkPosition(chunkTask.x(), chunkTask.y(), chunkTask.z()));
        sendChunkBytes(chunkTask.ctx(), PackageID.CHUNK_RESPONSE, chunkTask.x(), chunkTask.y(), chunkTask.z(), chunk);
    }

    public BlockingQueue<ChunkTask> getChunkTasks() {
        return chunkTasks;
    }
}