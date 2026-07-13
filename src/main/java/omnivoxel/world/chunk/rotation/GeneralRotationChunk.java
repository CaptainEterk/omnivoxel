package omnivoxel.world.chunk.rotation;

import omnivoxel.common.settings.ConstantCommonSettings;

public class GeneralRotationChunk implements RotationChunk {
    private final byte[] rotation;

    public GeneralRotationChunk() {
        rotation = new byte[ConstantCommonSettings.BLOCKS_IN_CHUNK];
    }

    public GeneralRotationChunk(ModifiedRotationChunk modifiedRotationChunk, int index, byte value) {
        this();
        for (int i = 0; i < ConstantCommonSettings.BLOCKS_IN_CHUNK; i++) {
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
}