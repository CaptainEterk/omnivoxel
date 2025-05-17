package omnivoxel.world.chunk;

import omnivoxel.world.block.Block;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.util.IndexCalculator;

public class ChunkFactory {
    public static Chunk create(Block[] blocks, Block[] palette) {
        if (palette.length == 1) {
            return new SingleBlockChunk(palette[0]);
        }
        for (int x = 0; x < ConstantGameSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantGameSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantGameSettings.CHUNK_HEIGHT; y++) {
                    Block block = blocks[IndexCalculator.calculateBlockIndexPadded(x, y, z)];
                }
            }
        }
        return null;
    }
}