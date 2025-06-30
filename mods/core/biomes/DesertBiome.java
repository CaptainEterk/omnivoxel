package core.biomes;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;

public class DesertBiome extends Biome {
    public DesertBiome(ServerBlockService blockService) {
        super(blockService);
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z, int yOffset, ServerBlockService blockService) {
        return blockService.getBlock("core:sand_block");
    }
}