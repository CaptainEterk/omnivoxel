package omnivoxel.util.config;

import omnivoxel.client.game.settings.ConstantGameSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ConfigFile {
    private final String configName;

    public ConfigFile(String configName) {
        this.configName = configName;
    }

    public Map<String, Object> load() {
        // TODO: Complete this
        return null;
    }

    public void write(Map<String, Object> properties) throws IOException {
        Files.createDirectories(Path.of(ConstantGameSettings.CONFIG_LOCATION));
    }
}