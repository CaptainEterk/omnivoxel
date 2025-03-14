package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.server.client.block.ServerBlock;

public interface ServerWorldDataService {
    ServerBlock getBlockAt(ChunkPosition chunkPosition, int x, int y, int z);
}