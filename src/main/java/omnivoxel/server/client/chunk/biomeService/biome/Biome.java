package omnivoxel.server.client.chunk.biomeService.biome;

import omnivoxel.server.client.ServerItem;
import omnivoxel.server.client.chunk.blockService.BlockService;

public abstract class Biome {
    public Biome(BlockService blockService) {

    }

    public abstract ServerItem getBlock(int x, int y, int z, int yOffset, BlockService blockService);
}