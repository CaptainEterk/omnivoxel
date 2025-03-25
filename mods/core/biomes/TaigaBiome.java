package core.biomes;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.BlockService;

public class TaigaBiome extends Biome {
    private final Block dirtBlock;
    private final Block snowBlock;

    public TaigaBiome(BlockService blockService) {
        super(blockService);
        dirtBlock = blockService.getBlock("core:dirt_block", null);
        snowBlock = blockService.getBlock("core:snow_block", null);
    }

    @Override
    public Block getBlock(int x, int y, int z, int yOffset, BlockService blockService) {
        return Math.random() > 0.5 ? dirtBlock : snowBlock;
    }
}