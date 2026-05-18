package omnivoxel.common.settings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Settings {
    private final Map<String, String> settings = new HashMap<>();

    public void load(String configLocation) throws IOException {
        Path dir = Path.of(configLocation);
        Files.createDirectories(dir);

        File file = new File(dir.toFile(), "settings");

        if (!file.exists()) {
            try (BufferedOutputStream out =
                         new BufferedOutputStream(new FileOutputStream(file))) {
                out.write(ConstantClientSettings.DEFAULT_SETTING_CONTENTS.getBytes());
            }
        }

        settings.clear();

        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            String content = new String(in.readAllBytes());
            String[] lines = content.split("\\R");

            for (String line : lines) {
                line = line.trim();
                if (!line.contains("=")) continue;

                int idx = line.indexOf('=');
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();

                settings.put(key, value);
            }
        }
    }

    public String getSetting(String settingName, String defaultValue) {
        return settings.computeIfAbsent(settingName, k -> defaultValue);
    }

    public int getIntSetting(String settingName, int defaultValue) {
        String v = settings.computeIfAbsent(settingName, k -> String.valueOf(defaultValue));
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public float getFloatSetting(String settingName, float defaultValue) {
        String v = settings.computeIfAbsent(settingName, k -> String.valueOf(defaultValue));
        try {
            return Float.parseFloat(v);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBooleanSetting(String settingName, boolean defaultValue) {
        String v = settings.computeIfAbsent(settingName, k -> String.valueOf(defaultValue));
        return Boolean.parseBoolean(v);
    }
}