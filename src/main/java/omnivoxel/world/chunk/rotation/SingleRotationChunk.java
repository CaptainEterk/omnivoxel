package omnivoxel.world.chunk.rotation;

public class SingleRotationChunk implements RotationChunk {
    private final byte rotation;
    private final int lod;

    public SingleRotationChunk(byte rotation, int lod) {
        this.rotation = rotation;
        this.lod = lod;
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

    @Override
    public int getLOD() {
        return lod;
    }
}