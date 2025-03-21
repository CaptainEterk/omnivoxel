package core.biomes;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;

public class TaigaBiome implements Biome {
    @Override
    public Block getBlock(int yOffset) {
        return new ServerBlock(Math.random() > 0.5 ? "core:dirt_block" : "core:snow_block");
    }
}