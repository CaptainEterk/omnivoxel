package omnivoxel.world.chunk;

import omnivoxel.world.chunk.rotation.RotationChunk;
import omnivoxel.world.chunk.rotation.SingleRotationChunk;

public class SingleBlockChunk<B> implements Chunk<B> {
    private final B block;
    private final byte rotation;
    private final int lod;

    public SingleBlockChunk(B block, int lod) {
        this(block, (byte) 0, lod);
    }

    public SingleBlockChunk(B block, byte rotation, int lod) {
        this.block = block;
        this.rotation = (byte) (rotation & 3);
        this.lod = lod;
    }

    @Override
    public B getBlock(int x, int y, int z) {
        return block;
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        if (this.block == null) {
            return new SingleBlockChunk<>(block, rotation, lod);
        }
        if (this.block == block) {
            return this;
        }
        return new BiBlockChunk<>(this.block, rotation, lod).setBlock(x, y, z, block);
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
        return new BiBlockChunk<>(block, this.rotation, lod).setBlockRotation(x, y, z, rotation);
    }

    @Override
    public RotationChunk getRotationChunk() {
        return new SingleRotationChunk(rotation, lod);
    }

    @Override
    public int getLOD() {
        return lod;
    }
}
