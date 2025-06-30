package core.biomes;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;

public class TempCaveBiome extends Biome {
    private final ServerBlock air;

    public TempCaveBiome(ServerBlockService blockService) {
        super(blockService);
        this.air = blockService.getBlock("omnivoxel:air");
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z, int yOffset, ServerBlockService blockService) {
        return air;
    }
}