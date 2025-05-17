package omnivoxel.server.chunk.result;

import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.client.block.ServerBlock;
import org.jetbrains.annotations.NotNull;

public class GeneralGeneratedChunk extends GeneratedChunk {
    private final ServerBlock[] blocks;

    public GeneralGeneratedChunk() {
        blocks = new ServerBlock[ConstantGameSettings.BLOCKS_IN_CHUNK_PADDED];
    }

    private static int calculateIndex(int x, int y, int z) {
        return (x + 1) * (ConstantGameSettings.CHUNK_WIDTH + 2) * (ConstantGameSettings.CHUNK_LENGTH + 2) + (z + 1) * (ConstantGameSettings.CHUNK_LENGTH + 2) + (y + 1);
    }

    protected ServerBlock getBlock(int x, int y, int z) {
        return blocks[calculateIndex(x, y, z)];
    }

    public GeneratedChunk setBlock(int x, int y, int z, @NotNull ServerBlock block) {
        blocks[calculateIndex(x, y, z)] = block;
        return this;
    }
}