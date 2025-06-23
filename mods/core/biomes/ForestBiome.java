package core.biomes;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.BlockService;

public class ForestBiome extends Biome {
    private final ServerBlock grass;
    private final ServerBlock dirt;

    public ForestBiome(BlockService blockService) {
        super(blockService);
        grass = blockService.getBlock("core:grass_block", null);
        dirt = blockService.getBlock("core:dirt_block", null);
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z, int yOffset, BlockService blockService) {
        return yOffset == 0 ? grass : dirt;
    }
}