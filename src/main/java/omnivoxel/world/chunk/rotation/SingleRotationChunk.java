package omnivoxel.world.chunk.rotation;

public class SingleRotationChunk implements RotationChunk {
    private final byte rotation;

    public SingleRotationChunk(byte rotation) {
        this.rotation = rotation;
    }

    @Override
    public byte getRotation(int index) {
        return rotation;
    }

    @Override
    public RotationChunk setRotation(int index, byte value) {
        if (value == rotation) {
            return this;
        } else {
            return new ModifiedRotationChunk(this, index, value);
        }
    }
}