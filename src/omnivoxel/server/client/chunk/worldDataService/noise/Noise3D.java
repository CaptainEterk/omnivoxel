package omnivoxel.server.client.chunk.worldDataService.noise;

public class Noise3D {
    private final FastNoiseLite noise;

    public Noise3D(String noiseType) {
        noise = new FastNoiseLite();
        noise.SetNoiseType(FastNoiseLite.NoiseType.valueOf(noiseType));
    }

    public float generate(float x, float y, float z) {
        return noise.GetNoise(x, y, z);
    }
}