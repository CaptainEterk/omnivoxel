package omnivoxel.server.world;

import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.Position3D;
import omnivoxel.server.client.block.Block;
import omnivoxel.server.world.chunk.ByteChunk;
import omnivoxel.server.world.chunk.Chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BasicWorld implements World {
    private final Map<Position3D, Block> queuedBlocks;
    private final Map<Position3D, Chunk> chunks;

    public BasicWorld() {
        queuedBlocks = new ConcurrentHashMap<>();
        chunks = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isBlockQueued(Position3D position3D) {
        return queuedBlocks.containsKey(position3D);
    }

    @Override
    public Block takeQueuedBlock(Position3D position3D) {
        return queuedBlocks.remove(position3D);
    }

    @Override
    public void setBlock(Position3D position3D, Block block) {
        long chunkX = Math.floorDiv(position3D.x(), ConstantGameSettings.CHUNK_WIDTH);
        long chunkY = Math.floorDiv(position3D.y(), ConstantGameSettings.CHUNK_HEIGHT);
        long chunkZ = Math.floorDiv(position3D.z(), ConstantGameSettings.CHUNK_LENGTH);
        Position3D chunkPos = new Position3D(chunkX, chunkY, chunkZ);
        if (chunks.keySet().stream().anyMatch(pos -> pos.equals(chunkPos))) {
//            System.out.println("Chunk exists!");
            // Send a block update in the chunk
        } else {
            queuedBlocks.put(position3D, block);
            // TODO: Instead of queueing blocks, generate the chunk and set the block in it
        }
    }

    @Override
    public void addChunk(Position3D position3D, Chunk chunk) {
        // TODO: Make it so this method actually works.
//        this.chunks.put(position3D, chunk);
    }

    @Override
    public Chunk getChunk(Position3D position3D) {
        return chunks.get(position3D);
    }
}