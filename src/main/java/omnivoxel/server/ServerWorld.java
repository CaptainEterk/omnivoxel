package omnivoxel.server;

import omnivoxel.math.Position3D;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.world.chunk.Chunk;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerWorld {
    private final Map<Position3D, Chunk> chunks;

    public ServerWorld() {
        chunks = new ConcurrentHashMap<>();
    }

    public void setBlock(Position3D position3D, ServerBlock block) {
        if (block == null) {
            return;
        }
//        int chunkX = Math.floorDiv(position3D.x(), ConstantGameSettings.CHUNK_WIDTH);
//        int chunkY = Math.floorDiv(position3D.y(), ConstantGameSettings.CHUNK_HEIGHT);
//        int chunkZ = Math.floorDiv(position3D.z(), ConstantGameSettings.CHUNK_LENGTH);
//        Position3D chunkPos = new Position3D(chunkX, chunkY, chunkZ);
//        if (chunks.keySet().stream().anyMatch(pos -> pos.equals(chunkPos))) {
////            System.out.println("Chunk exists!");
//            // Send a block update in the chunk
//        } else {
//        queuedBlocks.put(position3D, block);
//        }
    }

    public void add(Position3D position3D, Chunk chunk) {
//        this.chunks.put(position3D, chunk);
    }

    public Chunk get(Position3D position3D) {
        return chunks.get(position3D);
    }
}