package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.IndexCalculator;
import omnivoxel.world.chunk.rotation.RotationChunk;
import omnivoxel.world.chunk.rotation.SingleRotationChunk;

import java.util.Objects;

public class BiBlockChunk<B> implements Chunk<B> {
    private final int[] blocks;

    private final B block1;
    private B block2;

    private final byte block1Rotation;
    private final int lod;

    private final int width;
    private final int length;

    private RotationChunk rotationChunk;

    public BiBlockChunk(B block, int lod) {
        this(block, (byte) 0, lod);
    }

    public BiBlockChunk(B block, byte rotation, int lod) {
        this.block1 = block;
        this.block1Rotation = (byte) (rotation & 3);
        this.lod = lod;

        this.width = ConstantCommonSettings.CHUNK_WIDTH >> lod;
        this.length = ConstantCommonSettings.CHUNK_LENGTH >> lod;

        this.blocks = new int[width * length];

        this.rotationChunk = new SingleRotationChunk(rotation, lod);
    }

    private int index(int x, int z) {
        return x + z * width;
    }

    private int lx(int x) {
        return x >> lod;
    }

    private int ly(int y) {
        return y >> lod;
    }

    private int lz(int z) {
        return z >> lod;
    }

    @Override
    public B getBlock(int x, int y, int z) {
        int lx = lx(x);
        int ly = ly(y);
        int lz = lz(z);

        return (blocks[index(lx, lz)] & (1 << ly)) != 0
                ? block2
                : block1;
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        int lx = lx(x);
        int ly = ly(y);
        int lz = lz(z);

        int index = index(lx, lz);
        int mask = 1 << ly;

        if (Objects.equals(block1, block)) {
            blocks[index] &= ~mask;
        } else {
            if (block2 == null) {
                block2 = block;
            }

            if (Objects.equals(block2, block)) {
                blocks[index] |= mask;
            } else {
                return new ModifiedChunk<>(x, y, z, block, this);
            }
        }

        rotationChunk = rotationChunk.setRotation(
                IndexCalculator.calculateBlockIndex(lx, ly, lz),
                block1Rotation
        );

        return this;
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        int lx = lx(x);
        int ly = ly(y);
        int lz = lz(z);

        if ((blocks[index(lx, lz)] & (1 << ly)) == 0) {
            return block1Rotation;
        }

        return rotationChunk.getRotation(
                IndexCalculator.calculateBlockIndex(lx, ly, lz)
        );
    }

    @Override
    public Chunk<B> setBlockRotation(int x, int y, int z, byte rotation) {
        rotationChunk = rotationChunk.setRotation(
                IndexCalculator.calculateBlockIndex(
                        lx(x),
                        ly(y),
                        lz(z)
                ),
                (byte) (rotation & 3)
        );

        return this;
    }

    @Override
    public RotationChunk getRotationChunk() {
        return rotationChunk;
    }

    @Override
    public int getLOD() {
        return lod;
    }
}