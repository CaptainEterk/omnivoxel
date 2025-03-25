package core.biomes;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.BlockService;

public class PlainsBiome extends Biome {
    public PlainsBiome(BlockService blockService) {
        super(blockService);
    }

    @Override
    public Block getBlock(int yOffset, BlockService blockService) {
        return blockService.getBlock("core:grass_block", null);
    }
}