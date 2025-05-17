package omnivoxel.world.chunk;

import omnivoxel.world.block.Block;

public interface Chunk {
    Block getBlock(int x, int y, int z);

    Chunk setBlock(int x, int y, int z, Block block);
}