package omnivoxel.server;

public final class ConstantServerSettings {
    public static final int CHUNK_GENERATOR_INDIVIDUAL_TASK_COUNT = 50;
    public static final int CHUNK_GENERATOR_THREAD_LIMIT = Runtime.getRuntime().availableProcessors();
    public static final int CHUNK_REQUEST_LIMIT = CHUNK_GENERATOR_THREAD_LIMIT * CHUNK_GENERATOR_INDIVIDUAL_TASK_COUNT;
    public static final int QUEUED_CHUNKS_MINIMUM = 10000;
    public static final int CHUNK_MODIFICATION_GENERALIZATION_LIMIT = 200;
    public static final long CHUNK_REQUEST_BATCHING_TIME = 50;
    public static final int CHUNK_REQUEST_BATCHING_LIMIT = 1000;
}