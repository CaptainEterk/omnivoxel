package omnivoxel.server;

public final class ConstantServerSettings {
    public static final int CHUNK_GENERATOR_INDIVIDUAL_TASK_COUNT = 5;
    public static final int CHUNK_GENERATOR_THREAD_LIMIT = Runtime.getRuntime().availableProcessors();
    public static final int CHUNK_REQUEST_LIMIT = CHUNK_GENERATOR_THREAD_LIMIT * CHUNK_GENERATOR_INDIVIDUAL_TASK_COUNT;
    public static final int INFLIGHT_REQUESTS_MINIMUM = CHUNK_REQUEST_LIMIT / 5;
    public static final int CHUNK_MODIFICATION_GENERALIZATION_LIMIT = 10;
    public static final long CHUNK_REQUEST_BATCHING_TIME = 50;
    public static final int CHUNK_REQUEST_BATCHING_LIMIT = 1000;
    public static final int CHUNK_TIME_LIMIT = 10;

    public static final String WORLD_SAVE_LOCATION = "./world/";
    public static final String CHUNK_SAVE_LOCATION = WORLD_SAVE_LOCATION + "chunks/";
    public static final String GAME_LOCATION = "game/";
    public static final String CACHE_LOCATION = WORLD_SAVE_LOCATION + "cache/";
    public static final String HEIGHT_LOCATION = CACHE_LOCATION + "heights/";
}