package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.IndexCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortPaletteChunk<B> implements Chunk<B> {
    private final short[] blocks;
    private final byte[] rotations;
    private final List<B> palette;
    private final Map<B, Short> paletteIndex;

    public ShortPaletteChunk() {
        this.blocks = new short[ConstantCommonSettings.BLOCKS_IN_CHUNK];
        this.rotations = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK];
        this.palette = new ArrayList<>();
        this.paletteIndex = new HashMap<>();
    }

    public ShortPaletteChunk(Chunk<B> chunk) {
        this.palette = new ArrayList<>();
        this.paletteIndex = new HashMap<>();
        this.blocks = extractBlocks(chunk);
        this.rotations = extractRotations(chunk);
    }

    public List<B> getPalette() {
        return palette;
    }

    private short[] extractBlocks(Chunk<B> chunk) {
        short[] blocks = new short[ConstantCommonSettings.BLOCKS_IN_CHUNK];

        for (int x = 0; x < ConstantCommonSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantCommonSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantCommonSettings.CHUNK_HEIGHT; y++) {
                    B block = chunk.getBlock(x, y, z);

                    Short index = paletteIndex.get(block);
                    if (index == null) {
                        index = (short) palette.size();
                        palette.add(block);
                        paletteIndex.put(block, index);
                    }

                    blocks[IndexCalculator.calculateBlockIndex(x, y, z)] = index;
                }
            }
        }

        return blocks;
    }

    private byte[] extractRotations(Chunk<B> chunk) {
        byte[] rotations = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK];

        for (int x = 0; x < ConstantCommonSettings.CHUNK_WIDTH; x++) {
            for (int z = 0; z < ConstantCommonSettings.CHUNK_LENGTH; z++) {
                for (int y = 0; y < ConstantCommonSettings.CHUNK_HEIGHT; y++) {
                    rotations[IndexCalculator.calculateBlockIndex(x, y, z)] = chunk.getBlockRotation(x, y, z);
                }
            }
        }

        return rotations;
    }

    @Override
    public B getBlock(int x, int y, int z) {
        return palette.get(blocks[IndexCalculator.calculateBlockIndex(x, y, z)]);
    }

    @Override
    public Chunk<B> setBlock(int x, int y, int z, B block) {
        int blockIndex = IndexCalculator.calculateBlockIndex(x, y, z);

        Short index = paletteIndex.get(block);
        if (index == null) {
            index = (short) palette.size();
            palette.add(block);
            paletteIndex.put(block, index);
        }

        blocks[blockIndex] = index;
        rotations[blockIndex] = 0;

        if (palette.size() > Short.MAX_VALUE - 2) {
            return new IntPaletteChunk<>(this);
        }

        return this;
    }

    @Override
    public byte getBlockRotation(int x, int y, int z) {
        return rotations[IndexCalculator.calculateBlockIndex(x, y, z)];
    }

    @Override
    public Chunk<B> setBlockRotation(int x, int y, int z, byte rotation) {
        rotations[IndexCalculator.calculateBlockIndex(x, y, z)] = (byte) (rotation & 3);
        return this;
    }
}
