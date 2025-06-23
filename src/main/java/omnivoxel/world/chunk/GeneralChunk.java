package omnivoxel.world.chunk;

import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.util.IndexCalculator;

public class GeneralChunk implements Chunk {
    private final ServerBlock[] blocks;

    public GeneralChunk() {
        this.blocks = new ServerBlock[ConstantGameSettings.BLOCKS_IN_CHUNK];
    }

    public GeneralChunk(Chunk chunk) {
        this.blocks = GeneralChunk.extractBlocks(chunk);
    }

    public static ServerBlock[] extractBlocks(Chunk chunk) {
        ServerBlock[] blocks = new ServerBlock[ConstantGameSettings.BLOCKS_IN_CHUNK];
        for (int x = 0; x < ConstantGameSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantGameSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantGameSettings.CHUNK_HEIGHT; y++) {
                    blocks[IndexCalculator.calculateBlockIndex(x, y, z)] = chunk.getBlock(x, y, z);
                }
            }
        }
        return blocks;
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z) {
        return blocks[IndexCalculator.calculateBlockIndex(x, y, z)];
    }

    @Override
    public Chunk setBlock(int x, int y, int z, ServerBlock block) {
        try {
            blocks[IndexCalculator.calculateBlockIndex(x, y, z)] = block;
            return this;
        } catch (Exception e) {
            System.out.println(x + " " + y + " " + z);
            throw new RuntimeException(e);
        }
    }
}