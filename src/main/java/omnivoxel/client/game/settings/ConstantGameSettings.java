package omnivoxel.client.game.settings;

public class ConstantGameSettings {
    public static final int CHUNK_WIDTH = 32;
    public static final int CHUNK_HEIGHT = 32;
    public static final int CHUNK_LENGTH = 32;

    public static final int PADDED_WIDTH = ConstantGameSettings.CHUNK_WIDTH + 2;
    public static final int PADDED_HEIGHT = ConstantGameSettings.CHUNK_HEIGHT + 2;
    public static final int PADDED_LENGTH = ConstantGameSettings.CHUNK_LENGTH + 2;

    public static final int BLOCKS_IN_CHUNK = ((ConstantGameSettings.CHUNK_WIDTH) * (ConstantGameSettings.CHUNK_LENGTH) * (ConstantGameSettings.CHUNK_HEIGHT));
    public static final int BLOCKS_IN_CHUNK_PADDED = PADDED_WIDTH*PADDED_HEIGHT*PADDED_LENGTH;
    // TODO: Move this to settings
    public static final int BLOCK_CACHE_SIZE = 1000;
    public static final int NON_GENERATED_QUEUE_OVERLOAD_LIMIT = 100;

    // TODO: Move this to settings
    public static final int TARGET_FPS = 120;
    public static final float FRAME_TIME = 1f / TARGET_FPS;

    public static final String DEFAULT_WINDOW_TITLE = "OmniVoxel v0.7-alpha";

    // TODO: Move this to settings
    public static final int MAX_MESH_GENERATOR_THREADS = Runtime.getRuntime().availableProcessors();
    public static final int BUFFERIZE_CHUNKS_PER_FRAME = 10000;

    public static final String FILE_LOCATION = getRootFolder();

    public static final String CONFIG_LOCATION = FILE_LOCATION + ".config/";
    public static final String DEFAULT_SETTING_CONTENTS = """
            width=750
            height=750
            render_distance=128""";

    public static final long AUTO_RECALCULATE_CHUNKS_TIME = 5000;
    public static final String DATA_LOCATION = "";
    public static final String LOG_LOCATION = FILE_LOCATION + ".logs/";

    private static String getRootFolder() {
        return System.getProperty("user.dir") + "/.omnivoxel/";
    }
}