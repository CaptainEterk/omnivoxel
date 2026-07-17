package omnivoxel.world.chunk;

import omnivoxel.common.settings.ConstantCommonSettings;
import omnivoxel.util.IndexCalculator;
import omnivoxel.world.chunk.rotation.RotationChunk;
import omnivoxel.world.chunk.rotation.SingleRotationChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntPaletteChunk<B> implements Chunk<B> {
    private final int[] blocks;
    private final RotationChunk rotations;
    private final List<B> palette;
    private final Map<B, Integer> paletteIndex;
    private final int lod;

    public IntPaletteChunk(Chunk<B> chunk, int lod) {
        this.lod = lod;
        this.palette = new ArrayList<>();
        this.paletteIndex = new HashMap<>();
        this.blocks = extractBlocks(chunk, lod);
        this.rotations = extractRotations(chunk, lod);
    }

    private int[] extractBlocks(Chunk<B> chunk, int lod) {
        if (chunk.getLOD() != lod) {
            throw new IllegalStateException("Chunk has wrong lod: " + chunk.getLOD());
        }

        int[] blocks = new int[ConstantCommonSettings.BLOCKS_IN_CHUNK >> lod >> lod >> lod];

        for (int x = 0; x < ConstantCommonSettings.CHUNK_WIDTH >> lod; x++) {
            for (int z = 0; z < ConstantCommonSettings.CHUNK_LENGTH >> lod; z++) {
                for (int y = 0; y < ConstantCommonSettings.CHUNK_HEIGHT >> lod; y++) {

                    B block = chunk.getBlock(x, y, z);

                    Integer index = paletteIndex.get(block);
                    if (index == null) {
                        index = palette.size();
                        palette.add(block);
                        paletteIndex.put(block, index);
                    }

                    blocks[IndexCalculator.calculateBlockIndex(x, y, z)] = index;
                }
            }
        }

        return blocks;
    }

    private RotationChunk extractRotations(Chunk<B> chunk, int lod) {
        if (chunk.getRotationChunk() != null) {
            return chunk.getRotationChunk();
        }

        RotationChunk rotations = new SingleRotationChunk((byte) 0, lod);

        for (int x = 0; x < ConstantCommonSettings.CHUNK_WIDTH >> lod; x++) {
            for (int z = 0; z < ConstantCommonSettings.CHUNK_LENGTH >> lod; z++) {
                for (int y = 0; y < ConstantCommonSettings.CHUNK_HEIGHT >> lod; y++) {
                    rotations.setRotation(IndexCalculator.calculateBlockIndex(x, y, z), chunk.getBlockRotation(x, y, z));
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

        Integer index = paletteIndex.get(block);
        if (index == null) {
            index = palette.size();
            palette.add(block);
            paletteIndex.put(block, index);
        }

        blocks[blockIndex] = index;
        rotations.setRotation(blockIndex, (byte) 0);
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
