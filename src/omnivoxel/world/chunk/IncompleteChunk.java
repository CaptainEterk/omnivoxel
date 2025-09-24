package omnivoxel.world.chunk;

import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.common.face.BlockFace;

public class IncompleteChunk<B> implements Chunk<B> {
    private final B[] blocks;
    private final BlockFace face;
    private final int side = ConstantGameSettings.BLOCKS_IN_CHUNK_SIDE;

    @SuppressWarnings("unchecked")
    public IncompleteChunk(BlockFace face) {
        // only need side*side elements for one face
        this.blocks = (B[]) new Object[side * side];
        this.face = face;
    }

    @Override
    public B getBlock(int x, int y, int z) {
        int index = getIndexIfOnFace(x, y, z);
        if (index == -1) return null;
        return blocks[index];
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        int index = getIndexIfOnFace(x, y, z);
        if (index != -1) {
            blocks[index] = block;
        }
        return this;
    }

    private int getIndexIfOnFace(int x, int y, int z) {
        switch (face) {
            case EAST:
                if (x != side - 1) return -1;
                return y * side + z;
            case WEST:
                if (x != 0) return -1;
                return y * side + z;
            case TOP:
                if (y != side - 1) return -1;
                return z * side + x;
            case BOTTOM:
                if (y != 0) return -1;
                return z * side + x;
            case NORTH:
                if (z != side - 1) return -1;
                return y * side + x;
            case SOUTH:
                if (z != 0) return -1;
                return y * side + x;
            default:
                return -1;
        }
    }
}