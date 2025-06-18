package core.biomes;

import omnivoxel.server.client.ServerItem;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.BlockService;

public class PlainsBiome extends Biome {
    public PlainsBiome(BlockService blockService) {
        super(blockService);
    }

    @Override
    public ServerItem getBlock(int x, int y, int z, int yOffset, BlockService blockService) {
        return yOffset == 0 ? blockService.getBlock("core:grass_block", null) : blockService.getBlock("core:dirt_block", null);
    }
}