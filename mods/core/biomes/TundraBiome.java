package core.biomes;

import core.structures.StoneObeliskStructure;
import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.StructureSeed;
import omnivoxel.server.client.chunk.biomeService.biome.Biome;
import omnivoxel.server.client.chunk.blockService.BlockService;
import omnivoxel.server.client.chunk.worldDataService.BasicWorldDataService;

public class TundraBiome extends Biome {
    private final StructureSeed structure = new StructureSeed(new StoneObeliskStructure());

    public TundraBiome(BlockService blockService) {
        super(blockService);
        structure.structure().initBlocks();
    }

    @Override
    public Block getBlock(int x, int y, int z, int yOffset, BlockService blockService) {
        if (yOffset == 0 && Math.random() > 0.99 && y > BasicWorldDataService.WATER_LEVEL) {
            return structure;
        } else {
            return blockService.getBlock("core:snow_block", null);
        }
    }
}