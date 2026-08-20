package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.world.chunk.rotation.RotationChunk;

public class ChunkShell<B> implements Chunk<B> {
    private final Object[] minX;
    private final Object[] maxX;
    private final byte[] minXRotations;
    private final byte[] maxXRotations;

    private final Object[] minY;
    private final Object[] maxY;
    private final byte[] minYRotations;
    private final byte[] maxYRotations;

    private final Object[] minZ;
    private final Object[] maxZ;
    private final byte[] minZRotations;
    private final byte[] maxZRotations;

    private final int width;
    private final int height;
    private final int length;

    private final int lod;

    public ChunkShell(int lod) {
        this.lod = lod;

        width = ConstantCommonSettings.CHUNK_WIDTH >> lod;
        height = ConstantCommonSettings.CHUNK_HEIGHT >> lod;
        length = ConstantCommonSettings.CHUNK_LENGTH >> lod;

        minX = new Object[height * length];
        maxX = new Object[height * length];
        minXRotations = new byte[height * length];
        maxXRotations = new byte[height * length];

        minY = new Object[width * length];
        maxY = new Object[width * length];
        minYRotations = new byte[width * length];
        maxYRotations = new byte[width * length];

        minZ = new Object[width * height];
        maxZ = new Object[width * height];
        minZRotations = new byte[width * height];
        maxZRotations = new byte[width * height];
    }

    public ChunkShell(ChunkShell<B> other) {
        this.lod = other.lod;

        width = other.width;
        height = other.height;
        length = other.length;

        minX = other.minX.clone();
        maxX = other.maxX.clone();
        minXRotations = other.minXRotations.clone();
        maxXRotations = other.maxXRotations.clone();

        minY = other.minY.clone();
        maxY = other.maxY.clone();
        minYRotations = other.minYRotations.clone();
        maxYRotations = other.maxYRotations.clone();

        minZ = other.minZ.clone();
        maxZ = other.maxZ.clone();
        minZRotations = other.minZRotations.clone();
        maxZRotations = other.maxZRotations.clone();
    }

    private int indexXZ(int x, int z) {
        return x + z * width;
    }

    private int indexYZ(int y, int z) {
        return y + z * height;
    }

    private int indexXY(int x, int y) {
        return x + y * width;
    }

    @SuppressWarnings("unchecked")
    @Override
    public B getBlock(int x, int y, int z) {
        if (x == 0) {
            return (B) minX[indexYZ(y, z)];
        }

        if (x == width - 1) {
            return (B) maxX[indexYZ(y, z)];
        }

        if (y == 0) {
            return (B) minY[indexXZ(x, z)];
        }

        if (y == height - 1) {
            return (B) maxY[indexXZ(x, z)];
        }

        if (z == 0) {
            return (B) minZ[indexXY(x, y)];
        }

        if (z == length - 1) {
            return (B) maxZ[indexXY(x, y)];
        }

        throw new IllegalArgumentException("Attempted to access interior block of ChunkShell");
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        if (x == 0) {
            minX[indexYZ(y, z)] = block;
            return this;
        }

        if (x == width - 1) {
            maxX[indexYZ(y, z)] = block;
            return this;
        }

        if (y == 0) {
            minY[indexXZ(x, z)] = block;
            return this;
        }

        if (y == height - 1) {
            maxY[indexXZ(x, z)] = block;
            return this;
        }

        if (z == 0) {
            minZ[indexXY(x, y)] = block;
            return this;
        }

        if (z == length - 1) {
            maxZ[indexXY(x, y)] = block;
            return this;
        }

        throw new UnsupportedOperationException("Cannot set interior block in ChunkShell (" + x + ", " + y + ", " + z + ")");
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        if (x == 0) {
            return minXRotations[indexYZ(y, z)];
        }

        if (x == width - 1) {
            return maxXRotations[indexYZ(y, z)];
        }

        if (y == 0) {
            return minYRotations[indexXZ(x, z)];
        }

        if (y == height - 1) {
            return maxYRotations[indexXZ(x, z)];
        }

        if (z == 0) {
            return minZRotations[indexXY(x, y)];
        }

        if (z == length - 1) {
            return maxZRotations[indexXY(x, y)];
        }

        throw new IllegalArgumentException("Attempted to access interior block of ChunkShell");
    }

    @Override
    public Chunk<B> setBlockRotation(int x, int y, int z, byte rotation) {
        rotation &= 3;

        if (x == 0) {
            minXRotations[indexYZ(y, z)] = rotation;
            return this;
        }

        if (x == width - 1) {
            maxXRotations[indexYZ(y, z)] = rotation;
            return this;
        }

        if (y == 0) {
            minYRotations[indexXZ(x, z)] = rotation;
            return this;
        }

        if (y == height - 1) {
            maxYRotations[indexXZ(x, z)] = rotation;
            return this;
        }

        if (z == 0) {
            minZRotations[indexXY(x, y)] = rotation;
            return this;
        }

        if (z == length - 1) {
            maxZRotations[indexXY(x, y)] = rotation;
            return this;
        }

        throw new UnsupportedOperationException("Cannot set interior block rotation in ChunkShell");
    }

    @Override
    public RotationChunk getRotationChunk() {
        return null;
    }

    public ChunkShell<B> mergeDown(ChunkShell<B> newShell) {
        if (newShell.lod > lod) {
            return newShell.mergeDown(this);
        }

        ChunkShell<B> result = new ChunkShell<>(this);

        if (newShell.lod < lod) {
            int scale = 1 << (lod - newShell.lod);

            for (int z = 0; z < length; z++) {
                for (int y = 0; y < height; y++) {
                    int fy = y * scale;
                    int fz = z * scale;

                    B block = newShell.getBlock(0, fy, fz);
                    if (block != null) {
                        result.setBlock(0, y, z, block);
                        result.setBlockRotation(0, y, z, newShell.getBlockRotation(0, fy, fz));
                    }

                    block = newShell.getBlock(newShell.width - 1, fy, fz);
                    if (block != null) {
                        result.setBlock(width - 1, y, z, block);
                        result.setBlockRotation(width - 1, y, z,
                                newShell.getBlockRotation(newShell.width - 1, fy, fz));
                    }
                }
            }

            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    int fx = x * scale;
                    int fz = z * scale;

                    B block = newShell.getBlock(fx, 0, fz);
                    if (block != null) {
                        result.setBlock(x, 0, z, block);
                        result.setBlockRotation(x, 0, z, newShell.getBlockRotation(fx, 0, fz));
                    }

                    block = newShell.getBlock(fx, newShell.height - 1, fz);
                    if (block != null) {
                        result.setBlock(x, height - 1, z, block);
                        result.setBlockRotation(x, height - 1, z,
                                newShell.getBlockRotation(fx, newShell.height - 1, fz));
                    }
                }
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int fx = x * scale;
                    int fy = y * scale;

                    B block = newShell.getBlock(fx, fy, 0);
                    if (block != null) {
                        result.setBlock(x, y, 0, block);
                        result.setBlockRotation(x, y, 0, newShell.getBlockRotation(fx, fy, 0));
                    }

                    block = newShell.getBlock(fx, fy, newShell.length - 1);
                    if (block != null) {
                        result.setBlock(x, y, length - 1, block);
                        result.setBlockRotation(x, y, length - 1,
                                newShell.getBlockRotation(fx, fy, newShell.length - 1));
                    }
                }
            }

            return result;
        }

        for (int z = 0; z < length; z++) {
            for (int y = 0; y < height; y++) {
                B block = newShell.getBlock(0, y, z);
                if (block != null) {
                    result.setBlock(0, y, z, block);
                    result.setBlockRotation(0, y, z, newShell.getBlockRotation(0, y, z));
                }

                block = newShell.getBlock(width - 1, y, z);
                if (block != null) {
                    result.setBlock(width - 1, y, z, block);
                    result.setBlockRotation(width - 1, y, z,
                            newShell.getBlockRotation(width - 1, y, z));
                }
            }
        }

        for (int z = 0; z < length; z++) {
            for (int x = 0; x < width; x++) {
                B block = newShell.getBlock(x, 0, z);
                if (block != null) {
                    result.setBlock(x, 0, z, block);
                    result.setBlockRotation(x, 0, z, newShell.getBlockRotation(x, 0, z));
                }

                block = newShell.getBlock(x, height - 1, z);
                if (block != null) {
                    result.setBlock(x, height - 1, z, block);
                    result.setBlockRotation(x, height - 1, z, newShell.getBlockRotation(x, height - 1, z));
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                B block = newShell.getBlock(x, y, 0);
                if (block != null) {
                    result.setBlock(x, y, 0, block);
                    result.setBlockRotation(x, y, 0, newShell.getBlockRotation(x, y, 0));
                }

                block = newShell.getBlock(x, y, length - 1);
                if (block != null) {
                    result.setBlock(x, y, length - 1, block);
                    result.setBlockRotation(x, y, length - 1,
                            newShell.getBlockRotation(x, y, length - 1));
                }
            }
        }

        return result;
    }

    @Override
    public int getLOD() {
        return lod;
    }

    public ChunkShell<B> mergeUp(ChunkShell<B> newShell) {
        if (newShell.lod >= lod) {
            throw new IllegalArgumentException(
                    "mergeUp requires a finer target LOD"
            );
        }

        ChunkShell<B> result = new ChunkShell<>(newShell);

        int scale = 1 << (lod - newShell.lod);

        // X borders
        for (int z = 0; z < newShell.length; z++) {
            for (int y = 0; y < newShell.height; y++) {
                int cy = y / scale;
                int cz = z / scale;

                B block = getBlock(0, cy, cz);
                if (block != null) {
                    result.setBlock(0, y, z, block);
                    result.setBlockRotation(
                            0, y, z,
                            getBlockRotation(0, cy, cz)
                    );
                }

                block = getBlock(width - 1, cy, cz);
                if (block != null) {
                    result.setBlock(
                            newShell.width - 1, y, z, block
                    );
                    result.setBlockRotation(
                            newShell.width - 1, y, z,
                            getBlockRotation(width - 1, cy, cz)
                    );
                }
            }
        }

        // Y borders
        for (int z = 0; z < newShell.length; z++) {
            for (int x = 0; x < newShell.width; x++) {
                int cx = x / scale;
                int cz = z / scale;

                B block = getBlock(cx, 0, cz);
                if (block != null) {
                    result.setBlock(x, 0, z, block);
                    result.setBlockRotation(
                            x, 0, z,
                            getBlockRotation(cx, 0, cz)
                    );
                }

                block = getBlock(cx, height - 1, cz);
                if (block != null) {
                    result.setBlock(
                            x, newShell.height - 1, z, block
                    );
                    result.setBlockRotation(
                            x, newShell.height - 1, z,
                            getBlockRotation(cx, height - 1, cz)
                    );
                }
            }
        }

        // Z borders
        for (int y = 0; y < newShell.height; y++) {
            for (int x = 0; x < newShell.width; x++) {
                int cx = x / scale;
                int cy = y / scale;

                B block = getBlock(cx, cy, 0);
                if (block != null) {
                    result.setBlock(x, y, 0, block);
                    result.setBlockRotation(
                            x, y, 0,
                            getBlockRotation(cx, cy, 0)
                    );
                }

                block = getBlock(cx, cy, length - 1);
                if (block != null) {
                    result.setBlock(
                            x, y, newShell.length - 1, block
                    );
                    result.setBlockRotation(
                            x, y, newShell.length - 1,
                            getBlockRotation(cx, cy, length - 1)
                    );
                }
            }
        }

        return result;
    }

    public Chunk<B> merge(ChunkShell<B> newChunkData) {
        return newChunkData.lod >= lod ? mergeDown(newChunkData) : mergeUp(newChunkData);
    }
}