package omnivoxel.server;

public class ConstantServerSettings {
    public static final int CHUNK_GENERATOR_INDIVIDUAL_TASK_COUNT = 25;
    public static final int CHUNK_GENERATOR_THREAD_LIMIT = Runtime.getRuntime().availableProcessors();
    public static final int CHUNK_REQUEST_LIMIT = CHUNK_GENERATOR_THREAD_LIMIT * CHUNK_GENERATOR_INDIVIDUAL_TASK_COUNT;
    public static final int CHUNK_MODIFICATION_GENERALIZATION_LIMIT = 100;
}