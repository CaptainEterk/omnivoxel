package omnivoxel.server.world.chunk;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;

public class EmptyChunk implements Chunk {
    private final static Block air = new ServerBlock("air");

    @Override
    public Block getBlock(int x, int y, int z) {
        return air;
    }

    @Override
    public Chunk setBlock(int x, int y, int z, Block block) {
        return new ModifiedChunk(x, y, z, block, this);
    }
}