package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;

public final class ChunkLODSampler {
    private ChunkLODSampler() {
    }

    public static <B> Chunk<B> downsample(Chunk<B> chunk, int lod) {
        if (lod < chunk.getLOD()) {
            throw new IllegalArgumentException("Cannot downsample to a finer LOD.");
        }

        if (lod == chunk.getLOD()) {
            return chunk;
        }

        if (chunk instanceof ChunkShell<B> shell) {
            return new ChunkShell<B>(lod).merge(shell);
        }

        int scale = 1 << (lod - chunk.getLOD());

        int width = ConstantCommonSettings.CHUNK_WIDTH >> lod;
        int height = ConstantCommonSettings.CHUNK_HEIGHT >> lod;
        int length = ConstantCommonSettings.CHUNK_LENGTH >> lod;

        Chunk<B> result = new ShortPaletteChunk<>(lod);

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = 0; y < height; y++) {
                    int sourceX = x * scale;
                    int sourceY = y * scale;
                    int sourceZ = z * scale;

                    result = result.setBlock(
                            x,
                            y,
                            z,
                            chunk.getBlock(sourceX, sourceY, sourceZ),
                            chunk.getBlockRotation(sourceX, sourceY, sourceZ)
                    );
                }
            }
        }

        return result;
    }

    public static <B> Chunk<B> upsample(Chunk<B> chunk, int lod) {
        if (lod > chunk.getLOD()) {
            throw new IllegalArgumentException("Cannot upsample to a coarser LOD.");
        }

        if (lod == chunk.getLOD()) {
            return chunk;
        }

        if (chunk instanceof ChunkShell<B> shell) {
            return shell.mergeUp(new ChunkShell<>(lod));
        }

        int scale = 1 << (chunk.getLOD() - lod);

        int width = ConstantCommonSettings.CHUNK_WIDTH >> lod;
        int height = ConstantCommonSettings.CHUNK_HEIGHT >> lod;
        int length = ConstantCommonSettings.CHUNK_LENGTH >> lod;

        Chunk<B> result = new ShortPaletteChunk<>(lod);

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                for (int y = 0; y < height; y++) {
                    int sourceX = x / scale;
                    int sourceY = y / scale;
                    int sourceZ = z / scale;

                    result = result.setBlock(
                            x,
                            y,
                            z,
                            chunk.getBlock(sourceX, sourceY, sourceZ),
                            chunk.getBlockRotation(sourceX, sourceY, sourceZ)
                    );
                }
            }
        }

        return result;
    }

    public static <B> Chunk<B> sample(Chunk<B> chunk, int lod) {
        if (lod == -1 || lod == chunk.getLOD()) {
            return chunk;
        }

        return lod < chunk.getLOD()
                ? upsample(chunk, lod)
                : downsample(chunk, lod);
    }
}