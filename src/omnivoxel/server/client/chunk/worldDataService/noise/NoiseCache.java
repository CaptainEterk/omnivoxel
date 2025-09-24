package omnivoxel.server.client.chunk.worldDataService.noise;

import omnivoxel.server.ServerLogger;

import java.util.HashMap;
import java.util.Map;

public class NoiseCache {
    private static final Map<String, Noise3D> noises = new HashMap<>();

    public static void registerNoise(String id, Noise3D noise3D) {
        noises.put(id, noise3D);
    }

    public static Noise3D get(String id) {
        Noise3D out = noises.get(id);
        if (out == null) {
            ServerLogger.logger.warn("Noise not found: " + id);
        }
        return out;
    }
}