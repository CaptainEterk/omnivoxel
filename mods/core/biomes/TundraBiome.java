package core.biomes;

import omnivoxel.server.client.ServerItem;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.BlockService;

public class TundraBiome extends Biome {
    public TundraBiome(BlockService blockService) {
        super(blockService);
    }

    @Override
    public ServerItem getBlock(int x, int y, int z, int yOffset, BlockService blockService) {
        return yOffset == 0 ? blockService.getBlock("core:snow_block", null) : blockService.getBlock("core:snow_dirt_block", null);
    }
}