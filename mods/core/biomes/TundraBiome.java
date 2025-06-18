package core.biomes;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.BlockService;

public class TundraBiome extends Biome {
    public TundraBiome(BlockService blockService) {
        super(blockService);
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z, int yOffset, BlockService blockService) {
        return yOffset == 0 ? blockService.getBlock("core:snow_block", null) : blockService.getBlock("core:snow_dirt_block", null);
    }
}