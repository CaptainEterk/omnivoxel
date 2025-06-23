package omnivoxel.world.chunk;

import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.util.IndexCalculator;

public class PaddedChunkResult implements Chunk {
    private final ServerBlock[] blocks;

    public PaddedChunkResult() {
        this.blocks = new ServerBlock[ConstantGameSettings.BLOCKS_IN_CHUNK];
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z) {
        return blocks[IndexCalculator.calculateBlockIndex(x, y, z)];
    }

    @Override
    public Chunk setBlock(int x, int y, int z, ServerBlock block) {
        blocks[IndexCalculator.calculateBlockIndex(x, y, z)] = block;
        return this;
    }
}