package core.biomes;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;

public class TundraBiome extends Biome {
    public TundraBiome(ServerBlockService blockService) {
        super(blockService);
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z, int yOffset, ServerBlockService blockService) {
        return yOffset == 0 ? blockService.getBlock("core:snow_block", null) : blockService.getBlock("core:snow_grass_block");
    }
}