package omnivoxel.client.game.thread.mesh.util.noise;

public class FractionalBrownianNoise {

    private final PerlinNoise noise;
    private final int octaves;
    private final double persistence;
    private final double lacunarity;
    private final double frequency;

    // Constructor to set up noise parameters
    public FractionalBrownianNoise(
            PerlinNoise noise,
            int octaves,
            double persistence,
            double lacunarity,
            double frequency) {
        this.noise = noise;
        this.octaves = octaves;
        this.persistence = persistence;
        this.lacunarity = lacunarity;
        this.frequency = frequency;
    }

    // Generate fBm noise at a given point (x, z)
    public double generate(int x, int z) {
        double total = 0;
        double amplitude = 1;
        double maxValue = 0;
        double flatness = frequency;

        for (int i = 0; i < octaves; i++) {
            total += noise.perlin2d(x, z, flatness) * amplitude;

            maxValue += amplitude;
            amplitude *= persistence;   // Reduce amplitude for each octave
            flatness *= lacunarity;    // Increase flatness for each octave
        }

        return (total / maxValue);
    }
}