package core.biomes;

import omnivoxel.server.client.ServerItem;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.BlockService;

public class DesertBiome extends Biome {
    public DesertBiome(BlockService blockService) {
        super(blockService);
    }

    @Override
    public ServerItem getBlock(int x, int y, int z, int yOffset, BlockService blockService) {
        return blockService.getBlock("core:sand_block", null);
    }
}