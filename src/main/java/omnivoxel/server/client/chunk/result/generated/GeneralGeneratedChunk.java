package omnivoxel.server.client.chunk.result.generated;

import omnivoxel.common.annotations.NotNull;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.server.client.block.ServerBlock;

public class GeneralGeneratedChunk extends GeneratedChunk {
    private final ServerBlock[] blocks;
    private final byte[] rotations;
    private final int lod;
    private final int size;
    private final int paddedSize;

    public GeneralGeneratedChunk(int lod) {
        this.lod = lod;
        this.size = ConstantCommonSettings.CHUNK_WIDTH >> lod;
        this.paddedSize = size + 2;

        blocks = new ServerBlock[paddedSize * paddedSize * paddedSize];
        rotations = new byte[paddedSize * paddedSize * paddedSize];
    }

    public GeneralGeneratedChunk(GeneratedChunk chunk, int lod) {
        this.lod = lod;
        this.size = ConstantCommonSettings.CHUNK_WIDTH >> lod;
        this.paddedSize = size + 2;

        this.blocks = extractBlocks(chunk);
        this.rotations = extractRotations(chunk);
    }

    private ServerBlock[] extractBlocks(GeneratedChunk chunk) {
        ServerBlock[] blocks = new ServerBlock[paddedSize * paddedSize * paddedSize];

        for (int x = -1; x <= size; x++) {
            for (int z = -1; z <= size; z++) {
                for (int y = -1; y <= size; y++) {

                    int srcX = x << lod;
                    int srcY = y << lod;
                    int srcZ = z << lod;

                    blocks[index(x, y, z)] = chunk.getBlock(srcX, srcY, srcZ);
                }
            }
        }

        return blocks;
    }

    private byte[] extractRotations(GeneratedChunk chunk) {
        byte[] rotations = new byte[paddedSize * paddedSize * paddedSize];

        for (int x = -1; x <= size; x++) {
            for (int z = -1; z <= size; z++) {
                for (int y = -1; y <= size; y++) {

                    int srcX = x << lod;
                    int srcY = y << lod;
                    int srcZ = z << lod;

                    rotations[index(x, y, z)] = chunk.getBlockRotation(srcX, srcY, srcZ);
                }
            }
        }

        return rotations;
    }

    private int index(int x, int y, int z) {
        return ((x + 1) * paddedSize * paddedSize)
                + ((z + 1) * paddedSize)
                + (y + 1);
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z) {
        return blocks[index(x, y, z)];
    }

    @Override
    public GeneratedChunk setBlock(int x, int y, int z, @NotNull ServerBlock block) {
        blocks[index(x, y, z)] = block;
        rotations[index(x, y, z)] = 0;
        return this;
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        return rotations[index(x, y, z)];
    }

    @Override
    public GeneratedChunk setBlockRotation(int x, int y, int z, byte rotation) {
        rotations[index(x, y, z)] = (byte) (rotation & 3);
        return this;
    }

    public int getLOD() {
        return lod;
    }
}