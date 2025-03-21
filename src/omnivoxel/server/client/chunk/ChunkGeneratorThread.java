package omnivoxel.server.client.chunk;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.server.PackageID;
import omnivoxel.server.Position3D;
import omnivoxel.server.world.World;
import omnivoxel.server.world.chunk.ByteChunk;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChunkGeneratorThread implements Runnable {
    private final ChunkGenerator chunkGenerator;
    private final BlockingQueue<ChunkTask> chunkTasks;
    private final World world;

    public ChunkGeneratorThread(ChunkGenerator chunkGenerator, World world) {
        this.chunkGenerator = chunkGenerator;
        this.world = world;
        this.chunkTasks = new LinkedBlockingQueue<>();
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
                ChunkTask c = chunkTasks.take();
                Position3D position3D = new Position3D(c.x(), c.y(), c.z());
                ByteChunk chunk = world.getChunk(position3D);
                if (chunk == null) {
                    ByteChunk byteChunk = chunkGenerator.generateChunk(new ChunkPosition(c.x(), c.y(), c.z()));
                    sendChunkBytes(c.ctx(), c.x(), c.y(), c.z(), byteChunk.bytes());
                    world.addChunk(position3D, byteChunk);
                } else {
                    sendChunkBytes(c.ctx(), c.x(), c.y(), c.z(), chunk.bytes());
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public BlockingQueue<ChunkTask> getChunkTasks() {
        return chunkTasks;
    }
}