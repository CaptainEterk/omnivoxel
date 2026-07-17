package omnivoxel.world.chunk.rotation;

import omnivoxel.common.settings.ConstantCommonSettings;

public class GeneralRotationChunk implements RotationChunk {
    private final byte[] rotation;
    private final int lod;

    public GeneralRotationChunk(int lod) {
        this.lod = lod;
        rotation = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK >> lod];
    }

    public GeneralRotationChunk(ModifiedRotationChunk modifiedRotationChunk, int index, byte value, int lod) {
        this(lod);
        for (int i = 0; i < ConstantCommonSettings.BLOCKS_IN_CHUNK >> lod; i++) {
            if (index == i) {
                rotation[i] = value;
            } else {
                rotation[i] = modifiedRotationChunk.getRotation(i);
            }
        }
    }

    @Override
    public byte getRotation(int index) {
        return rotation[index];
    }

    @Override
    public RotationChunk setRotation(int index, byte value) {
        rotation[index] = value;
        return this;
    }

    @Override
    public int getLOD() {
        return lod;
    }
}