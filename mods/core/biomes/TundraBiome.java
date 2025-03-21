package core.biomes;

import core.structures.StoneObeliskStructure;
import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.block.StructureSeed;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;

public class TundraBiome implements Biome {
    private final StructureSeed structure = new StructureSeed(new StoneObeliskStructure());

    public TundraBiome() {
        structure.structure().initBlocks();
    }

    @Override
    public Block getBlock(int yOffset) {
        if (yOffset == 0 && Math.random() > 0.99) {
            return structure;
        } else {
            return new ServerBlock("core:snow_block");
        }
    }
}