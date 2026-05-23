package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;

// TODO: Memory optimizations
public class ChunkShell<B> implements Chunk<B> {
    private final Object[] minX = new Object[ConstantCommonSettings.CHUNK_HEIGHT * ConstantCommonSettings.CHUNK_LENGTH];
    private final Object[] maxX = new Object[ConstantCommonSettings.CHUNK_HEIGHT * ConstantCommonSettings.CHUNK_LENGTH];
    private final byte[] minXRotations = new byte[ConstantCommonSettings.CHUNK_HEIGHT * ConstantCommonSettings.CHUNK_LENGTH];
    private final byte[] maxXRotations = new byte[ConstantCommonSettings.CHUNK_HEIGHT * ConstantCommonSettings.CHUNK_LENGTH];

    private final Object[] minY = new Object[ConstantCommonSettings.CHUNK_WIDTH * ConstantCommonSettings.CHUNK_LENGTH];
    private final Object[] maxY = new Object[ConstantCommonSettings.CHUNK_WIDTH * ConstantCommonSettings.CHUNK_LENGTH];
    private final byte[] minYRotations = new byte[ConstantCommonSettings.CHUNK_WIDTH * ConstantCommonSettings.CHUNK_LENGTH];
    private final byte[] maxYRotations = new byte[ConstantCommonSettings.CHUNK_WIDTH * ConstantCommonSettings.CHUNK_LENGTH];

    private final Object[] minZ = new Object[ConstantCommonSettings.CHUNK_WIDTH * ConstantCommonSettings.CHUNK_HEIGHT];
    private final Object[] maxZ = new Object[ConstantCommonSettings.CHUNK_WIDTH * ConstantCommonSettings.CHUNK_HEIGHT];
    private final byte[] minZRotations = new byte[ConstantCommonSettings.CHUNK_WIDTH * ConstantCommonSettings.CHUNK_HEIGHT];
    private final byte[] maxZRotations = new byte[ConstantCommonSettings.CHUNK_WIDTH * ConstantCommonSettings.CHUNK_HEIGHT];

    // May return null if one of the sides has not been initialized
    @SuppressWarnings("unchecked")
    @Override
    public B getBlock(int x, int y, int z) {
        if (x == 0) {
            return (B) minX[y * ConstantCommonSettings.CHUNK_LENGTH + z];
        }

        if (x == ConstantCommonSettings.CHUNK_WIDTH - 1) {
            return (B) maxX[y * ConstantCommonSettings.CHUNK_LENGTH + z];
        }

        if (y == 0) {
            return (B) minY[x * ConstantCommonSettings.CHUNK_LENGTH + z];
        }

        if (y == ConstantCommonSettings.CHUNK_HEIGHT - 1) {
            return (B) maxY[x * ConstantCommonSettings.CHUNK_LENGTH + z];
        }

        if (z == 0) {
            return (B) minZ[x * ConstantCommonSettings.CHUNK_HEIGHT + y];
        }

        if (z == ConstantCommonSettings.CHUNK_LENGTH - 1) {
            return (B) maxZ[x * ConstantCommonSettings.CHUNK_HEIGHT + y];
        }

        throw new IllegalArgumentException("Attempted to access interior block of ChunkShell");
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {

        if (x == 0) {
            minX[y * ConstantCommonSettings.CHUNK_LENGTH + z] = block;
            return this;
        }

        if (x == ConstantCommonSettings.CHUNK_WIDTH - 1) {
            maxX[y * ConstantCommonSettings.CHUNK_LENGTH + z] = block;
            return this;
        }

        if (y == 0) {
            minY[x * ConstantCommonSettings.CHUNK_LENGTH + z] = block;
            return this;
        }

        if (y == ConstantCommonSettings.CHUNK_HEIGHT - 1) {
            maxY[x * ConstantCommonSettings.CHUNK_LENGTH + z] = block;
            return this;
        }

        if (z == 0) {
            minZ[x * ConstantCommonSettings.CHUNK_HEIGHT + y] = block;
            return this;
        }

        if (z == ConstantCommonSettings.CHUNK_LENGTH - 1) {
            maxZ[x * ConstantCommonSettings.CHUNK_HEIGHT + y] = block;
            return this;
        }

        throw new UnsupportedOperationException("Cannot set interior block in ChunkShell");
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        if (x == 0) {
            return minXRotations[y * ConstantCommonSettings.CHUNK_LENGTH + z];
        }

        if (x == ConstantCommonSettings.CHUNK_WIDTH - 1) {
            return maxXRotations[y * ConstantCommonSettings.CHUNK_LENGTH + z];
        }

        if (y == 0) {
            return minYRotations[x * ConstantCommonSettings.CHUNK_LENGTH + z];
        }

        if (y == ConstantCommonSettings.CHUNK_HEIGHT - 1) {
            return maxYRotations[x * ConstantCommonSettings.CHUNK_LENGTH + z];
        }

        if (z == 0) {
            return minZRotations[x * ConstantCommonSettings.CHUNK_HEIGHT + y];
        }

        if (z == ConstantCommonSettings.CHUNK_LENGTH - 1) {
            return maxZRotations[x * ConstantCommonSettings.CHUNK_HEIGHT + y];
        }

        throw new IllegalArgumentException("Attempted to access interior block of ChunkShell");
    }

    @Override
    public Chunk<B> setBlockRotation(int x, int y, int z, byte rotation) {
        rotation = (byte) (rotation & 3);

        if (x == 0) {
            minXRotations[y * ConstantCommonSettings.CHUNK_LENGTH + z] = rotation;
            return this;
        }

        if (x == ConstantCommonSettings.CHUNK_WIDTH - 1) {
            maxXRotations[y * ConstantCommonSettings.CHUNK_LENGTH + z] = rotation;
            return this;
        }

        if (y == 0) {
            minYRotations[x * ConstantCommonSettings.CHUNK_LENGTH + z] = rotation;
            return this;
        }

        if (y == ConstantCommonSettings.CHUNK_HEIGHT - 1) {
            maxYRotations[x * ConstantCommonSettings.CHUNK_LENGTH + z] = rotation;
            return this;
        }

        if (z == 0) {
            minZRotations[x * ConstantCommonSettings.CHUNK_HEIGHT + y] = rotation;
            return this;
        }

        if (z == ConstantCommonSettings.CHUNK_LENGTH - 1) {
            maxZRotations[x * ConstantCommonSettings.CHUNK_HEIGHT + y] = rotation;
            return this;
        }

        throw new UnsupportedOperationException("Cannot set interior block rotation in ChunkShell");
    }

    public void merge(ChunkShell<B> newShell) {
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
}
