package omnivoxel.world.chunk.rotation;

public interface RotationChunk {
    byte getRotation(int index);

    RotationChunk setRotation(int index, byte value);
}