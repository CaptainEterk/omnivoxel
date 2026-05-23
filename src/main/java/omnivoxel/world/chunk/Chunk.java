package omnivoxel.world.chunk;

public interface Chunk<B> {
    B getBlock(int x, int y, int z);

    Chunk<B> setBlock(int x, int y, int z, B block);

    byte getBlockRotation(int x, int y, int z);

    Chunk<B> setBlockRotation(int x, int y, int z, byte rotation);

    default Chunk<B> setBlock(int x, int y, int z, B block, byte rotation) {
        return setBlock(x, y, z, block).setBlockRotation(x, y, z, rotation);
    }
}
