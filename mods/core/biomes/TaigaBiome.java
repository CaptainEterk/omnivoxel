package core.biomes;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;

public class TaigaBiome extends Biome {
    private final ServerBlock dirtBlock;
    private final ServerBlock snowBlock;

    public TaigaBiome(ServerBlockService blockService) {
        super(blockService);
        dirtBlock = blockService.getBlock("core:dirt_block");
        snowBlock = blockService.getBlock("core:snow_block");
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z, int yOffset, ServerBlockService blockService) {
        return Math.random() > 0.5 ? dirtBlock : snowBlock;
    }
}