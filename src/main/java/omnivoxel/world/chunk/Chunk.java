package omnivoxel.world.chunk;

import omnivoxel.server.client.block.ServerBlock;

public interface Chunk {
    ServerBlock getBlock(int x, int y, int z);

    Chunk setBlock(int x, int y, int z, ServerBlock block);
}