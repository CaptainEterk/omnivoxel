package omnivoxel.server.client.chunk.result.generated;

import omnivoxel.common.annotations.NotNull;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.util.IndexCalculator;

public class GeneralGeneratedChunk extends GeneratedChunk {
    private final ServerBlock[] blocks;
    private final byte[] rotations;

    public GeneralGeneratedChunk() {
        blocks = new ServerBlock[ConstantCommonSettings.BLOCKS_IN_CHUNK_PADDED];
        rotations = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK_PADDED];
    }

    public GeneralGeneratedChunk(GeneratedChunk chunk) {
        this.blocks = extractBlocks(chunk);
        this.rotations = extractRotations(chunk);
    }

    private ServerBlock[] extractBlocks(GeneratedChunk chunk) {
        ServerBlock[] blocks = new ServerBlock[ConstantCommonSettings.BLOCKS_IN_CHUNK_PADDED];
        for (int x = -1; x <= ConstantCommonSettings.CHUNK_WIDTH; x++) {
            for (int z = -1; z <= ConstantCommonSettings.CHUNK_LENGTH; z++) {
                for (int y = -1; y <= ConstantCommonSettings.CHUNK_HEIGHT; y++) {
                    blocks[IndexCalculator.calculateBlockIndexPadded(x, y, z)] = chunk.getBlock(x, y, z);
                }
            }
        }
        return blocks;
    }

    private byte[] extractRotations(GeneratedChunk chunk) {
        byte[] rotations = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK_PADDED];
        for (int x = -1; x <= ConstantCommonSettings.CHUNK_WIDTH; x++) {
            for (int z = -1; z <= ConstantCommonSettings.CHUNK_LENGTH; z++) {
                for (int y = -1; y <= ConstantCommonSettings.CHUNK_HEIGHT; y++) {
                    rotations[IndexCalculator.calculateBlockIndexPadded(x, y, z)] = chunk.getBlockRotation(x, y, z);
                }
            }
        }
        return rotations;
    }

    public ServerBlock getBlock(int x, int y, int z) {
        return blocks[IndexCalculator.calculateBlockIndexPadded(x, y, z)];
    }

    public GeneratedChunk setBlock(int x, int y, int z, @NotNull ServerBlock block) {
        blocks[IndexCalculator.calculateBlockIndexPadded(x, y, z)] = block;
        rotations[IndexCalculator.calculateBlockIndexPadded(x, y, z)] = 0;
        return this;
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        return rotations[IndexCalculator.calculateBlockIndexPadded(x, y, z)];
    }

    @Override
    public GeneratedChunk setBlockRotation(int x, int y, int z, byte rotation) {
        rotations[IndexCalculator.calculateBlockIndexPadded(x, y, z)] = (byte) (rotation & 3);
        return this;
    }
}
