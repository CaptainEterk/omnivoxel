package omnivoxel.server.client.chunk.worldDataService;

public class ChunkInfo {
    private final Object[] info;

    public ChunkInfo(Object... info) {
        this.info = info;
    }

    public <T> T get(int i, Class<T> valueType) {
        Object value = info[i];
        if (valueType.isInstance(value)) {
            return valueType.cast(value);
        }
        throw new IllegalArgumentException("Invalid type");
    }
}