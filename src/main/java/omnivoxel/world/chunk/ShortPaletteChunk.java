package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.IndexCalculator;
import omnivoxel.world.chunk.rotation.RotationChunk;
import omnivoxel.world.chunk.rotation.SingleRotationChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortPaletteChunk<B> implements Chunk<B> {
    private final short[] blocks;
    private final RotationChunk rotations;
    private final List<B> palette;
    private final Map<B, Short> paletteIndex;
    private final int lod;

    public ShortPaletteChunk(int lod) {
        this.lod = lod;
        this.blocks = new short[ConstantCommonSettings.BLOCKS_IN_CHUNK >> (lod * 3)];
        this.rotations = new SingleRotationChunk((byte) 0, lod);
        this.palette = new ArrayList<>();
        this.paletteIndex = new HashMap<>();
    }

    public ShortPaletteChunk(Chunk<B> chunk, int lod) {
        this.lod = lod;
        this.palette = new ArrayList<>();
        this.paletteIndex = new HashMap<>();
        this.blocks = extractBlocks(chunk);
        this.rotations = extractRotations(chunk);
    }

    public List<B> getPalette() {
        return palette;
    }

    private short[] extractBlocks(Chunk<B> chunk) {
        short[] blocks = new short[ConstantCommonSettings.BLOCKS_IN_CHUNK >> (lod * 3)];

        for (int x = 0; x < ConstantCommonSettings.CHUNK_WIDTH >> lod; x++) {
            for (int z = 0; z < ConstantCommonSettings.CHUNK_LENGTH >> lod; z++) {
                for (int y = 0; y < ConstantCommonSettings.CHUNK_HEIGHT >> lod; y++) {
                    B block = chunk.getBlock(x, y, z);

                    Short index = paletteIndex.get(block);
                    if (index == null) {
                        index = (short) palette.size();
                        palette.add(block);
                        paletteIndex.put(block, index);
                    }

                    blocks[IndexCalculator.calculateBlockIndex(x, y, z, ConstantCommonSettings.CHUNK_WIDTH >> lod, ConstantCommonSettings.CHUNK_HEIGHT >> lod, ConstantCommonSettings.CHUNK_LENGTH >> lod)] = index;
                }
            }
        }

        return blocks;
    }

    private RotationChunk extractRotations(Chunk<B> chunk) {
        if (chunk.getRotationChunk() != null) {
            return chunk.getRotationChunk();
        }

        RotationChunk rotations = new SingleRotationChunk((byte) 0, lod);

        for (int x = 0; x < ConstantCommonSettings.CHUNK_WIDTH >> lod; x++) {
            for (int z = 0; z < ConstantCommonSettings.CHUNK_LENGTH >> lod; z++) {
                for (int y = 0; y < ConstantCommonSettings.CHUNK_HEIGHT >> lod; y++) {
                    rotations.setRotation(IndexCalculator.calculateBlockIndex(x, y, z, ConstantCommonSettings.CHUNK_WIDTH >> lod, ConstantCommonSettings.CHUNK_HEIGHT >> lod, ConstantCommonSettings.CHUNK_LENGTH >> lod), chunk.getBlockRotation(x, y, z));
                }
            }
        }

        return rotations;
    }

    @Override
    public B getBlock(int x, int y, int z) {
        return palette.get(blocks[IndexCalculator.calculateBlockIndex(x, y, z, ConstantCommonSettings.CHUNK_WIDTH >> lod, ConstantCommonSettings.CHUNK_HEIGHT >> lod, ConstantCommonSettings.CHUNK_LENGTH >> lod)]);
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        int blockIndex = IndexCalculator.calculateBlockIndex(x, y, z, ConstantCommonSettings.CHUNK_WIDTH >> lod, ConstantCommonSettings.CHUNK_HEIGHT >> lod, ConstantCommonSettings.CHUNK_LENGTH >> lod);

        Short index = paletteIndex.get(block);
        if (index == null) {
            index = (short) palette.size();
            palette.add(block);
            paletteIndex.put(block, index);
        }

        blocks[blockIndex] = index;
        rotations.setRotation(blockIndex, (byte) 0);

        if (palette.size() > Short.MAX_VALUE - 2) {
            return new IntPaletteChunk<>(this, lod);
        }

        return this;
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        return rotations.getRotation(IndexCalculator.calculateBlockIndex(x, y, z));
    }

    @Override
    public Chunk<B> setBlockRotation(int x, int y, int z, byte rotation) {
        rotations.setRotation(IndexCalculator.calculateBlockIndex(x, y, z), (byte) (rotation & 3));
        return this;
    }

    @Override
    public RotationChunk getRotationChunk() {
        return rotations;
    }

    @Override
    public int getLOD() {
        return lod;
    }
}
