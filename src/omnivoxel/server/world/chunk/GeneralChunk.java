package omnivoxel.server.world.chunk;

import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.client.block.Block;

public class GeneralChunk implements Chunk {
    private final Block[] blocks;

    public GeneralChunk() {
        this.blocks = new Block[ConstantGameSettings.BLOCKS_IN_CHUNK];
    }

    public GeneralChunk(Chunk chunk) {
        this.blocks = GeneralChunk.extractBlocks(chunk);
    }

    public static Block[] extractBlocks(Chunk chunk) {
        Block[] blocks = new Block[ConstantGameSettings.BLOCKS_IN_CHUNK];
        for (int x = 0; x < ConstantGameSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantGameSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantGameSettings.CHUNK_HEIGHT; y++) {
                    blocks[calculateIndex(x, y, z)] = chunk.getBlock(x, y, z);
                }
            }
        }
        return blocks;
    }

    private static int calculateIndex(int x, int y, int z) {
        return x * ConstantGameSettings.CHUNK_WIDTH * ConstantGameSettings.CHUNK_LENGTH + z * ConstantGameSettings.CHUNK_LENGTH + y;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return blocks[calculateIndex(x, y, z)];
    }

    @Override
    public Chunk setBlock(int x, int y, int z, Block block) {
        blocks[calculateIndex(x, y, z)] = block;
        return this;
    }
}