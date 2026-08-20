package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.world.chunk.rotation.RotationChunk;

public class ModifiedChunk<B> implements Chunk<B> {
    private final int x;
    private final int y;
    private final int z;
    private final Chunk<B> chunk;
    private final int modificationCount;
    private B block;
    private byte rotation;

    private ModifiedChunk(int x, int y, int z, B block, byte rotation, Chunk<B> chunk, int modificationCount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.rotation = (byte) (rotation & 3);
        this.chunk = chunk;
        this.modificationCount = modificationCount;
    }

    public ModifiedChunk(int x, int y, int z, B block, Chunk<B> chunk) {
        this(x, y, z, block, (byte) 0, chunk, 1);
    }

    @Override
    public B getBlock(int x, int y, int z) {
        if (x == this.x && y == this.y && z == this.z) {
            return block;
        }
        return chunk.getBlock(x, y, z);
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        if (x == this.x && y == this.y && z == this.z) {
            this.block = block;
        }
        if (modificationCount > ConstantCommonSettings.MODIFICATION_GENERALIZATION_LIMIT) {
            return new BytePaletteChunk<>(this, chunk.getLOD()).setBlock(x, y, z, block);
        }
        return new ModifiedChunk<>(x, y, z, block, (byte) 0, this, modificationCount + 1);
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        if (x == this.x && y == this.y && z == this.z) {
            return rotation;
        }
        return chunk.getBlockRotation(x, y, z);
    }

    @Override
    public Chunk<B> setBlockRotation(int x, int y, int z, byte rotation) {
        if (x == this.x && y == this.y && z == this.z) {
            this.rotation = rotation;
        }
        if (modificationCount > ConstantCommonSettings.MODIFICATION_GENERALIZATION_LIMIT) {
            return new BytePaletteChunk<>(this, chunk.getLOD()).setBlockRotation(x, y, z, rotation);
        }
        return new ModifiedChunk<>(x, y, z, getBlock(x, y, z), rotation, this, modificationCount + 1);
    }

    @Override
    public RotationChunk getRotationChunk() {
        return null;
    }

    @Override
    public int getLOD() {
        return chunk.getLOD();
    }
}
