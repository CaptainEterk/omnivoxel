package core.biomes;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;

public class PlainsBiome extends Biome {
    private final ServerBlock grass;
    private final ServerBlock dirt;

    public PlainsBiome(ServerBlockService blockService) {
        super(blockService);
        grass = blockService.getBlock("core:grass_block");
        dirt = blockService.getBlock("core:dirt_block");
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z, int yOffset, ServerBlockService blockService) {
        return yOffset == 0 ? grass : yOffset > -3 ? dirt : null;
    }
}