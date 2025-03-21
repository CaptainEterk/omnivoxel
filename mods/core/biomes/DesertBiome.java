package core.biomes;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;

public class DesertBiome implements Biome {
    @Override
    public Block getBlock(int yOffset) {
        return new ServerBlock("core:sand_block");
    }
}