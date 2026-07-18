package omnivoxel.server.client.chunk.result.generated;

import omnivoxel.common.annotations.NotNull;
import omnivoxel.server.client.block.ServerBlock;

public final class SingleBlockGeneratedChunk extends GeneratedChunk {
    private final ServerBlock serverBlock;
    private final byte rotation;
    private final int lod;

    public SingleBlockGeneratedChunk(ServerBlock serverBlock, byte rotation, int lod) {
        this.serverBlock = serverBlock;
        this.rotation = rotation;
        this.lod = lod;
    }

    @Override
    public ServerBlock getBlock(int x, int y, int z) {
        return serverBlock;
    }

    @Override
    public GeneratedChunk setBlock(int x, int y, int z, @NotNull ServerBlock block) {
        return new ModifiedGeneratedChunk(x, y, z, block, this);
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        return rotation;
    }

    @Override
    public GeneratedChunk setBlockRotation(int x, int y, int z, byte rotation) {
        return new ModifiedGeneratedChunk(x, y, z, serverBlock, rotation, this);
    }

    @Override
    public int getLOD() {
        return lod;
    }
}
