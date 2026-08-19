package omnivoxel.server.client.chunk.result.generated;

import omnivoxel.common.annotations.NotNull;
import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.server.client.block.ServerBlock;

public class ModifiedGeneratedChunk extends GeneratedChunk {
    private final int x;
    private final int y;
    private final int z;
    private final ServerBlock block;
    private final byte rotation;
    private final GeneratedChunk chunk;
    private final int modificationCount;

    private ModifiedGeneratedChunk(int x, int y, int z, ServerBlock block, byte rotation, GeneratedChunk chunk, int modificationCount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.block = block;
        this.rotation = (byte) (rotation & 3);
        this.chunk = chunk;
        this.modificationCount = modificationCount;
    }

    public ModifiedGeneratedChunk(int x, int y, int z, ServerBlock block, GeneratedChunk chunk) {
        this(x, y, z, block, (byte) 0, chunk, 1);
    }

    public ModifiedGeneratedChunk(int x, int y, int z, ServerBlock block, byte rotation, GeneratedChunk chunk) {
        this(x, y, z, block, rotation, chunk, 1);
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z) {
        if (x == this.x && y == this.y && z == this.z) {
            return block;
        }
        return chunk.getBlock(x, y, z);
    }

    @Override
    public GeneratedChunk setBlock(int x, int y, int z, @NotNull ServerBlock block) {
        if (modificationCount > ConstantCommonSettings.MODIFICATION_GENERALIZATION_LIMIT) {
            return new GeneralGeneratedChunk(this, chunk.getLOD());
        }
        return new ModifiedGeneratedChunk(x, y, z, block, (byte) 0, this, modificationCount + 1);
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        if (x == this.x && y == this.y && z == this.z) {
            return rotation;
        }
        return chunk.getBlockRotation(x, y, z);
    }

    @Override
    public GeneratedChunk setBlockRotation(int x, int y, int z, byte rotation) {
        if (modificationCount > ConstantCommonSettings.MODIFICATION_GENERALIZATION_LIMIT) {
            return new GeneralGeneratedChunk(this, chunk.getLOD()).setBlockRotation(x, y, z, rotation);
        }
        return new ModifiedGeneratedChunk(x, y, z, getBlock(x, y, z), rotation, this, modificationCount + 1);
    }

    @Override
    public int getLOD() {
        return chunk.getLOD();
    }

    public GeneratedChunk getChunk() {
        return chunk;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public ServerBlock getBlock() {
        return block;
    }

    public byte getRotation() {
        return rotation;
    }
}
