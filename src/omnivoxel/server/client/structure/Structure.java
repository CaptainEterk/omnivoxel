package omnivoxel.server.client.structure;

import omnivoxel.server.client.block.Block;

public interface Structure {
    void initBlocks();

    Block[][][] getBlocks();
}