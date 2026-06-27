package omnivoxel.common.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

// TODO: Make settings hot-reload
public final class Settings {

    private final Map<String, Setting> settings = new HashMap<>();

    public void load(String configLocation) throws IOException {
        Path directory = Path.of(configLocation);
        Files.createDirectories(directory);

        Path file = directory.resolve("settings");

        if (Files.notExists(file)) {
            Files.writeString(file, ConstantClientSettings.DEFAULT_SETTING_CONTENTS);
        }

        String content = Files.readString(file);

        for (String line : content.split("\\R")) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int equals = line.indexOf('=');
            if (equals < 0) {
                continue;
            }

            String key = line.substring(0, equals).trim();
            String value = line.substring(equals + 1).trim();

            settings.computeIfAbsent(key, k -> new Setting())
                    .setValue(value);
        }
    }

    public String getSetting(String name, String defaultValue) {
        return settings.computeIfAbsent(name, k -> new Setting(defaultValue))
                .getValue();
    }

    public int getIntSetting(String name, int defaultValue) {
        return settings.computeIfAbsent(name,
                        k -> new Setting(String.valueOf(defaultValue)))
                .getInt(defaultValue);
    }

    public float getFloatSetting(String name, float defaultValue) {
        return settings.computeIfAbsent(name,
                        k -> new Setting(String.valueOf(defaultValue)))
                .getFloat(defaultValue);
    }

    public boolean getBooleanSetting(String name, boolean defaultValue) {
        return settings.computeIfAbsent(name,
                        k -> new Setting(String.valueOf(defaultValue)))
                .getBoolean();
    }

    public void addSettingListener(String name, Consumer<String> listener) {
        settings.computeIfAbsent(name, k -> new Setting())
                .addListener(listener);
    }

    public void setSetting(String name, String value) {
        settings.computeIfAbsent(name, k -> new Setting())
                .setValue(value);
    }

    private static final class Setting {

        private String value;
        private final Set<Consumer<String>> listeners = new HashSet<>();

        private Setting() {
            this("");
        }

        private Setting(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getInt(int defaultValue) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        public float getFloat(float defaultValue) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        public boolean getBoolean() {
            return Boolean.parseBoolean(value);
        }

        public void addListener(Consumer<String> listener) {
            listeners.add(listener);
        }

        public void setValue(String value) {
            if (Objects.equals(this.value, value)) {
                return;
            }

            this.value = value;

            for (Consumer<String> listener : listeners) {
                listener.accept(value);
            }
        }
    }
}