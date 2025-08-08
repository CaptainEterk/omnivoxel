package omnivoxel.server.client.chunk.worldDataService;

import omnivoxel.server.client.chunk.worldDataService.density.functions.Noise3DDensityFunction;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import org.graalvm.polyglot.HostAccess;

public class WorldGenAPI {
    private final int seed;

    public WorldGenAPI(int seed) {
        this.seed = seed;
    }

    @HostAccess.Export
    public void registerNoise(String id, double[] amplitudes, double firstOctave) {
        Noise3DDensityFunction.noises.put(id, new Noise3D(amplitudes, firstOctave, seed));
    }
}