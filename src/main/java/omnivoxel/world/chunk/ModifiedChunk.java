package omnivoxel.world.chunk;

import omnivoxel.server.ConstantServerSettings;

public class ModifiedChunk<B> implements Chunk<B> {
    private final int x;
    private final int y;
    private final int z;
    private final B block;
    private final Chunk<B> chunk;
    private final int modificationCount;

    public ModifiedChunk(int x, int y, int z, B block, Chunk<B> chunk, int modificationCount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.chunk = chunk;
        this.modificationCount = modificationCount;
    }

    public ModifiedChunk(int x, int y, int z, B block, Chunk<B> chunk) {
        this(x, y, z, block, chunk, 1);
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
        if (modificationCount > ConstantServerSettings.CHUNK_MODIFICATION_GENERALIZATION_LIMIT) {
            return new GeneralChunk<>(this);
        }
        return new ModifiedChunk<>(x, y, z, block, this, modificationCount + 1);
    }
}