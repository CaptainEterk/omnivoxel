package omnivoxel.server;

public final class ConstantServerSettings {
    public static final int CHUNK_GENERATOR_INDIVIDUAL_TASK_COUNT = 10;
    public static final int CHUNK_GENERATOR_THREAD_LIMIT = Runtime.getRuntime().availableProcessors();
    public static final int CHUNK_REQUEST_LIMIT = CHUNK_GENERATOR_THREAD_LIMIT * CHUNK_GENERATOR_INDIVIDUAL_TASK_COUNT;
    public static final int CHUNK_MODIFICATION_GENERALIZATION_LIMIT = 100;
    public static final int QUEUED_CHUNKS_MINIMUM = 10;
    public static final long CHUNK_REQUEST_BATCHING_TIME = 100;
    public static final int CHUNK_REQUEST_BATCHING_LIMIT = 1000;
}