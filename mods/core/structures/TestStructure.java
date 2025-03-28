package core.structures;

import omnivoxel.server.client.block.Block;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.structure.Structure;

public class TestStructure implements Structure {
    private final Block[][][] blocks = new Block[10][10][10];

    @Override
    public void initBlocks() {
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 10; z++) {
                    blocks[x][y][z] = new ServerBlock("core:iron_block");
                }
            }
        }
    }

    @Override
    public Block[][][] getBlocks() {
        return blocks;
    }
}