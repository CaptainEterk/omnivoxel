package omnivoxel.world.chunk;

import omnivoxel.server.ConstantServerSettings;
import omnivoxel.server.client.block.ServerBlock;

public class ModifiedChunk implements Chunk {
    private final int x;
    private final int y;
    private final int z;
    private final ServerBlock block;
    private final Chunk chunk;
    private final int modificationCount;

    public ModifiedChunk(int x, int y, int z, ServerBlock block, Chunk chunk, int modificationCount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.chunk = chunk;
        this.modificationCount = modificationCount;
    }

    public ModifiedChunk(int x, int y, int z, ServerBlock block, Chunk chunk) {
        this(x, y, z, block, chunk, 1);
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z) {
        if (x == this.x && y == this.y && z == this.z) {
            return block;
        }
        return chunk.getBlock(x, y, z);
    }

    @Override
    public Chunk setBlock(int x, int y, int z, ServerBlock block) {
        if (modificationCount > ConstantServerSettings.CHUNK_MODIFICATION_GENERALIZATION_LIMIT) {
            return new GeneralChunk(this);
        }
        return new ModifiedChunk(x, y, z, block, this, modificationCount + 1);
    }
}