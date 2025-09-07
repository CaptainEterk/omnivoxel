package omnivoxel.server.client.chunk;

import omnivoxel.common.BlockShape;
import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.client.chunk.result.GeneralGeneratedChunk;
import omnivoxel.server.client.chunk.result.GeneratedChunk;
import org.jetbrains.annotations.NotNull;

public final class EmptyGeneratedChunk extends GeneratedChunk {
    public static final double[][] emptyUVCoords = new double[6][0];
    public static final ServerBlock air = new ServerBlock("omnivoxel:air/default", BlockShape.EMPTY_BLOCK_SHAPE_STRING, emptyUVCoords, true);

    @Override
    protected ServerBlock getBlock(int x, int y, int z) {
        return air;
    }

    @Override
    public GeneratedChunk setBlock(int x, int y, int z, @NotNull ServerBlock block) {
        GeneralGeneratedChunk generatedChunk = new GeneralGeneratedChunk();
        generatedChunk.setBlock(x, y, z, block);
        return generatedChunk;
    }
}