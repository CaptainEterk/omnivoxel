package omnivoxel.world.chunk;

import omnivoxel.world.block.Block;

public class SingleBlockChunk implements Chunk {
    private final Block block;

    public SingleBlockChunk(Block block) {
        this.block = block;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return block;
    }

    @Override
    public Chunk setBlock(int x, int y, int z, Block block) {
        return new ModifiedChunk(x, y, z, block, this);
    }
}
