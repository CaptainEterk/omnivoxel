package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.IndexCalculator;

public class PaddedChunkResult<B> implements Chunk<B> {
    private final B[] blocks;
    private final byte[] rotations;

    @SuppressWarnings("unchecked")
    public PaddedChunkResult() {
        this.blocks = (B[]) new Object[ConstantCommonSettings.BLOCKS_IN_CHUNK];
        this.rotations = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK];
    }

    @Override
    public B getBlock(int x, int y, int z) {
        return blocks[IndexCalculator.calculateBlockIndex(x, y, z)];
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        blocks[IndexCalculator.calculateBlockIndex(x, y, z)] = block;
        rotations[IndexCalculator.calculateBlockIndex(x, y, z)] = 0;
        return this;
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        return rotations[IndexCalculator.calculateBlockIndex(x, y, z)];
    }

    @Override
    public Chunk<B> setBlockRotation(int x, int y, int z, byte rotation) {
        rotations[IndexCalculator.calculateBlockIndex(x, y, z)] = (byte) (rotation & 3);
        return this;
    }
}
