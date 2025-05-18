package omnivoxel.server.client;

public record ServerEntity(byte[] id, float x, float y, float z, float rx, float ry, float rz) implements ServerItem {
    @Override
    public byte[] getBytes() {
        return null;
    }
}