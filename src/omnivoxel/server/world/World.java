package omnivoxel.server.world;

import omnivoxel.server.Position3D;
import omnivoxel.server.client.block.Block;
import omnivoxel.server.world.chunk.Chunk;

public interface World {
    Block takeQueuedBlock(Position3D blockPosition);

    void setBlock(Position3D position3D, Block block);

    void addChunk(Position3D position3D, Chunk chunk);

    Chunk getChunk(Position3D position3D);
}