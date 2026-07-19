package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.server.client.block.ServerBlock;

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
            ChunkShell<B> result = new ChunkShell<>(lod);
            return result.merge(shell);
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
            return upsampleShell(shell, lod);
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

    private static <B> ChunkShell<B> upsampleShell(ChunkShell<B> shell, int lod) {
        int scale = 1 << (shell.getLOD() - lod);

        ChunkShell<B> result = new ChunkShell<>(lod);

        int width = ConstantCommonSettings.CHUNK_WIDTH >> lod;
        int height = ConstantCommonSettings.CHUNK_HEIGHT >> lod;
        int length = ConstantCommonSettings.CHUNK_LENGTH >> lod;

        int oldWidth = ConstantCommonSettings.CHUNK_WIDTH >> shell.getLOD();
        int oldHeight = ConstantCommonSettings.CHUNK_HEIGHT >> shell.getLOD();
        int oldLength = ConstantCommonSettings.CHUNK_LENGTH >> shell.getLOD();


        // X faces
        for (int y = 0; y < oldHeight; y++) {
            for (int z = 0; z < oldLength; z++) {
                copyShellBlock(result, shell, 0, y, z, scale);
                copyShellBlock(result, shell, oldWidth - 1, y, z, scale);
            }
        }

        // Y faces
        for (int x = 0; x < oldWidth; x++) {
            for (int z = 0; z < oldLength; z++) {
                copyShellBlock(result, shell, x, 0, z, scale);
                copyShellBlock(result, shell, x, oldHeight - 1, z, scale);
            }
        }

        // Z faces
        for (int x = 0; x < oldWidth; x++) {
            for (int y = 0; y < oldHeight; y++) {
                copyShellBlock(result, shell, x, y, 0, scale);
                copyShellBlock(result, shell, x, y, oldLength - 1, scale);
            }
        }

        return result;
    }

    private static <B> void copyShellBlock(
            ChunkShell<B> target,
            ChunkShell<B> source,
            int x,
            int y,
            int z,
            int scale
    ) {
        B block = source.getBlock(x, y, z);

        if (block == null) {
            return;
        }

        byte rotation = source.getBlockRotation(x, y, z);

        int startX = x * scale;
        int startY = y * scale;
        int startZ = z * scale;

        int targetWidth = ConstantCommonSettings.CHUNK_WIDTH >> target.getLOD();
        int targetHeight = ConstantCommonSettings.CHUNK_HEIGHT >> target.getLOD();
        int targetLength = ConstantCommonSettings.CHUNK_LENGTH >> target.getLOD();

        if (x == 0 || x == (ConstantCommonSettings.CHUNK_WIDTH >> source.getLOD()) - 1) {
            int nx = (x == 0) ? 0 : targetWidth - 1;

            for (int dy = 0; dy < scale; dy++) {
                for (int dz = 0; dz < scale; dz++) {
                    int ny = startY + dy;
                    int nz = startZ + dz;

                    target.setBlock(nx, ny, nz, block);
                    target.setBlockRotation(nx, ny, nz, rotation);
                }
            }
        } else if (y == 0 || y == (ConstantCommonSettings.CHUNK_HEIGHT >> source.getLOD()) - 1) {
            int ny = (y == 0) ? 0 : targetHeight - 1;

            for (int dx = 0; dx < scale; dx++) {
                for (int dz = 0; dz < scale; dz++) {
                    int nx = startX + dx;
                    int nz = startZ + dz;

                    target.setBlock(nx, ny, nz, block);
                    target.setBlockRotation(nx, ny, nz, rotation);
                }
            }
        } else if (z == 0 || z == (ConstantCommonSettings.CHUNK_LENGTH >> source.getLOD()) - 1) {
            int nz = (z == 0) ? 0 : targetLength - 1;

            for (int dx = 0; dx < scale; dx++) {
                for (int dy = 0; dy < scale; dy++) {
                    int nx = startX + dx;
                    int ny = startY + dy;

                    target.setBlock(nx, ny, nz, block);
                    target.setBlockRotation(nx, ny, nz, rotation);
                }
            }
        }
    }

    public static <B> Chunk<B> sample(Chunk<B> chunk, int lod) {
        if (lod == chunk.getLOD() || lod == -1) {
            return chunk;
        } else if (lod < chunk.getLOD()) {
            return ChunkLODSampler.upsample(chunk, lod);
        } else {
            return ChunkLODSampler.downsample(chunk, lod);
        }
    }
}