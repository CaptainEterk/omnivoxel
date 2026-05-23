package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;

import java.util.Objects;

public class BiBlockChunk<B> implements Chunk<B> {
    private final int[] blocks;
    private final byte[] rotations;
    private final B block1;
    private B block2 = null;
    private final byte block1Rotation;

    public BiBlockChunk(B block) {
        this(block, (byte) 0);
    }

    public BiBlockChunk(B block, byte rotation) {
        this.block1 = block;
        this.block1Rotation = (byte) (rotation & 3);
        blocks = new int[ConstantCommonSettings.CHUNK_WIDTH * ConstantCommonSettings.CHUNK_LENGTH];
        rotations = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK];
    }

    @Override
    public B getBlock(int x, int y, int z) {
        return (blocks[z * ConstantCommonSettings.CHUNK_WIDTH + x] & (1 << y)) != 0 ? this.block2 : block1;
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        if (Objects.equals(this.block1, block)) {
            blocks[z * ConstantCommonSettings.CHUNK_WIDTH + x] &= ~(1 << y);
            rotations[omnivoxel.util.IndexCalculator.calculateBlockIndex(x, y, z)] = block1Rotation;
        } else if (this.block2 == null) {
            this.block2 = block;
            blocks[z * ConstantCommonSettings.CHUNK_WIDTH + x] |= (1 << y);
            rotations[omnivoxel.util.IndexCalculator.calculateBlockIndex(x, y, z)] = 0;
        } else if (Objects.equals(this.block2, block)) {
            blocks[z * ConstantCommonSettings.CHUNK_WIDTH + x] |= (1 << y);
            rotations[omnivoxel.util.IndexCalculator.calculateBlockIndex(x, y, z)] = 0;
        } else {
            return new ModifiedChunk<>(x, y, z, block, this);
        }
        return this;
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        if ((blocks[z * ConstantCommonSettings.CHUNK_WIDTH + x] & (1 << y)) == 0) {
            return block1Rotation;
        }
        return rotations[omnivoxel.util.IndexCalculator.calculateBlockIndex(x, y, z)];
    }

    @Override
    public Chunk<B> setBlockRotation(int x, int y, int z, byte rotation) {
        rotations[omnivoxel.util.IndexCalculator.calculateBlockIndex(x, y, z)] = (byte) (rotation & 3);
        return this;
    }
}
