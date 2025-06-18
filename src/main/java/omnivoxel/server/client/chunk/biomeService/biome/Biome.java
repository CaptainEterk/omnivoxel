package omnivoxel.server.client.chunk.biomeService.biome;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.blockService.BlockService;

public abstract class Biome {
    public Biome(BlockService blockService) {

    }

    public abstract ServerBlock getBlock(int x, int y, int z, int yOffset, BlockService blockService);
}