package core.biomes;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.BlockService;

public class DesertBiome extends Biome {
    public DesertBiome (BlockService blockService) {
        super(blockService);
    }

    @Override
    public Block getBlock(int yOffset, BlockService blockService) {
        return blockService.getBlock("core:sand_block", null);
    }
}