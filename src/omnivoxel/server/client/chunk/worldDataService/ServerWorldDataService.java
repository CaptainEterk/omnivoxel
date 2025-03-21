package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.client.game.position.ChunkPosition;
import omnivoxel.server.client.block.Block;
import org.jetbrains.annotations.NotNull;

public interface ServerWorldDataService {
    @NotNull Block getBlockAt(ChunkPosition chunkPosition, int x, int y, int z);
}