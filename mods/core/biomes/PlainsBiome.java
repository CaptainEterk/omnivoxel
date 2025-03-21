package core.biomes;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;

public class PlainsBiome implements Biome {
    @Override
    public Block getBlock(int yOffset) {
        return new ServerBlock("core:grass_block");
    }
}