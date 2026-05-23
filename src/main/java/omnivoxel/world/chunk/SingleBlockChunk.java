package omnivoxel.world.chunk;

public class SingleBlockChunk<B> implements Chunk<B> {
    private final B block;
    private final byte rotation;

    public SingleBlockChunk(B block) {
        this(block, (byte) 0);
    }

    public SingleBlockChunk(B block, byte rotation) {
        this.block = block;
        this.rotation = (byte) (rotation & 3);
    }

    @Override
    public B getBlock(int x, int y, int z) {
        return block;
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        if (this.block == block) {
            return this;
        }
        return new BiBlockChunk<>(this.block, rotation).setBlock(x, y, z, block);
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        return rotation;
    }

    @Override
    public Chunk<B> setBlockRotation(int x, int y, int z, byte rotation) {
        rotation = (byte) (rotation & 3);
        if (this.rotation == rotation) {
            return this;
        }
        return new BiBlockChunk<>(block, this.rotation).setBlockRotation(x, y, z, rotation);
    }
}
