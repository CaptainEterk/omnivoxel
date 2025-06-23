package omnivoxel.world.chunk;

import omnivoxel.server.client.block.ServerBlock;

public class SingleBlockChunk implements Chunk {
    private final ServerBlock block;

    public SingleBlockChunk(ServerBlock block) {
        this.block = block;
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z) {
        return block;
    }

    @Override
    public Chunk setBlock(int x, int y, int z, ServerBlock block) {
        return new ModifiedChunk(x, y, z, block, this);
    }
}