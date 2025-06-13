package omnivoxel.client.launcher;

import omnivoxel.client.game.settings.ConstantGameSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OmniVoxel {
    public static void init() throws IOException {
        createFileLocations();
    }

    private static void createFileLocations() throws IOException {
        // TODO: Make this initialize more than just the LOG_LOCATION
        Files.createDirectories(Path.of(ConstantGameSettings.LOG_LOCATION));
    }
}