package omnivoxel.server.client.chunk.biomeService.biome;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.blockService.ServerBlockService;

public abstract class Biome {
    public Biome(ServerBlockService blockService) {

    }

    public abstract ServerBlock getBlock(int x, int y, int z, int yOffset, ServerBlockService blockService);
}