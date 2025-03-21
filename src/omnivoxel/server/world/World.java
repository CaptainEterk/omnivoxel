package omnivoxel.server.world;

import omnivoxel.server.Position3D;
import omnivoxel.server.client.block.Block;
import omnivoxel.server.world.chunk.ByteChunk;

public interface World {
    boolean isBlockQueued(Position3D position3D);

    Block takeQueuedBlock(Position3D position3D);

    void setBlock(Position3D add, Block block);

    void addChunk(Position3D position3D, ByteChunk chunk);

    ByteChunk getChunk(Position3D position3D);
}