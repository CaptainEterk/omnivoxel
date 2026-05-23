package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.IndexCalculator;

public class GeneralChunk<B> implements Chunk<B> {
    private final B[] blocks;
    private final byte[] rotations;

    @SuppressWarnings("unchecked")
    public GeneralChunk() {
        this.blocks = (B[]) new Object[ConstantCommonSettings.BLOCKS_IN_CHUNK];
        this.rotations = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK];
    }

    public GeneralChunk(Chunk<B> chunk) {
        this.blocks = extractBlocks(chunk);
        this.rotations = extractRotations(chunk);
    }

    @SuppressWarnings("unchecked")
    private B[] extractBlocks(Chunk<B> chunk) {
        B[] blocks = (B[]) new Object[ConstantCommonSettings.BLOCKS_IN_CHUNK];
        for (int x = 0; x < ConstantCommonSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantCommonSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantCommonSettings.CHUNK_HEIGHT; y++) {
                    blocks[IndexCalculator.calculateBlockIndex(x, y, z)] = chunk.getBlock(x, y, z);
                }
            }
        }
        return blocks;
    }

    private byte[] extractRotations(Chunk<B> chunk) {
        byte[] rotations = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK];
        for (int x = 0; x < ConstantCommonSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantCommonSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantCommonSettings.CHUNK_HEIGHT; y++) {
                    rotations[IndexCalculator.calculateBlockIndex(x, y, z)] = chunk.getBlockRotation(x, y, z);
                }
            }
        }
        return rotations;
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
