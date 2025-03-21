package core.structures;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.structure.Structure;

public class StoneObeliskStructure implements Structure {
    private final Block[][][] blocks = new Block[1][10][1];

    @Override
    public void initBlocks() {
        for (int y = 0; y < 10; y++) {
            blocks[0][y][0] = new ServerBlock("core:stone_block");
        }
    }

    @Override
    public Block[][][] getBlocks() {
        return blocks;
    }
}