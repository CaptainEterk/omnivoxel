package omnivoxel.world.chunk.rotation;

import omnivoxel.common.settings.ConstantCommonSettings;

public class ModifiedRotationChunk implements RotationChunk {
    private final RotationChunk rotationChunk;
    private final int index;
    private final int modificationCount;
    private byte value;

    private ModifiedRotationChunk(RotationChunk rotationChunk, int index, byte value, int modificationCount) {
        this.rotationChunk = rotationChunk;
        this.index = index;
        this.value = value;
        this.modificationCount = modificationCount;
    }

    public ModifiedRotationChunk(RotationChunk rotationChunk, int index, byte value) {
        this(rotationChunk, index, value, 0);
    }

    @Override
    public byte getRotation(int index) {
        return index == this.index ? value : rotationChunk.getRotation(index);
    }

    @Override
    public RotationChunk setRotation(int index, byte value) {
        if (index == this.index) {
            this.value = value;
            return this;
        } else if (modificationCount < ConstantCommonSettings.MODIFICATION_GENERALIZATION_LIMIT) {
            return new ModifiedRotationChunk(this, index, value, modificationCount + 1);
        } else {
            return new GeneralRotationChunk(this, index, value);
        }
    }
}
