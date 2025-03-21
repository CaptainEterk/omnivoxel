package omnivoxel.server.client.chunk.biomeService.biome;

import omnivoxel.server.client.block.Block;

public interface Biome {
    Block getBlock(int yOffset);
}