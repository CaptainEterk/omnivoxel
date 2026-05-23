package omnivoxel.server.client.chunk.result.generated;

import omnivoxel.common.annotations.NotNull;
import omnivoxel.server.client.block.ServerBlock;


// TODO: Turn this into SingleBlockGeneratedChunk
public final class EmptyGeneratedChunk extends GeneratedChunk {
    @Override
    public ServerBlock getBlock(int x, int y, int z) {
        return ServerBlock.AIR;
    }

    @Override
    public GeneratedChunk setBlock(int x, int y, int z, @NotNull ServerBlock block) {
        return new ModifiedGeneratedChunk(x, y, z, block, this);
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        return 0;
    }

    @Override
    public GeneratedChunk setBlockRotation(int x, int y, int z, byte rotation) {
        return new ModifiedGeneratedChunk(x, y, z, ServerBlock.AIR, rotation, this);
    }
}
