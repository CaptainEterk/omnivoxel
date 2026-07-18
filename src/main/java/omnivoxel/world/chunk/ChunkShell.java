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

    public void merge(ChunkShell<B> newShell) {
        if (newShell.lod != lod) {
            throw new IllegalArgumentException("Cannot merge ChunkShells with different LODs");
        }

        for (int i = 0; i < minX.length; i++) {
            if (newShell.minX[i] != null) {
                minX[i] = newShell.minX[i];
                minXRotations[i] = newShell.minXRotations[i];
            }
            if (newShell.maxX[i] != null) {
                maxX[i] = newShell.maxX[i];
                maxXRotations[i] = newShell.maxXRotations[i];
            }
        }

        for (int i = 0; i < minY.length; i++) {
            if (newShell.minY[i] != null) {
                minY[i] = newShell.minY[i];
                minYRotations[i] = newShell.minYRotations[i];
            }
            if (newShell.maxY[i] != null) {
                maxY[i] = newShell.maxY[i];
                maxYRotations[i] = newShell.maxYRotations[i];
            }
        }

        for (int i = 0; i < minZ.length; i++) {
            if (newShell.minZ[i] != null) {
                minZ[i] = newShell.minZ[i];
                minZRotations[i] = newShell.minZRotations[i];
            }
            if (newShell.maxZ[i] != null) {
                maxZ[i] = newShell.maxZ[i];
                maxZRotations[i] = newShell.maxZRotations[i];
            }
        }
    }

    @Override
    public int getLOD() {
        return lod;
    }
}