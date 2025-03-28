package core.noise._2D;

import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;

public class FractionalBrownianNoise2D implements Noise2D {
    private final Noise2D noise;
    private final int octaves;
    private final double persistence;
    private final double lacunarity;
    private final double frequency;

    // Constructor to set up noise parameters
    public FractionalBrownianNoise2D(
            Noise2D noise,
            int octaves,
            double persistence,
            double lacunarity,
            double frequency
    ) {
        this.noise = noise;
        this.octaves = octaves;
        this.persistence = persistence;
        this.lacunarity = lacunarity;
        this.frequency = frequency;
    }

    @Override
    public double generate(double x, double z) {
        double total = 0;
        double currentX = x * frequency;
        double currentZ = z * frequency;

        double amplitudeSum = 0; // Precompute normalization factor
        double currentAmplitude = 1;

        for (int i = 0; i < octaves; i++) {
            total += noise.generate(currentX, currentZ) * currentAmplitude;
            amplitudeSum += currentAmplitude;

            currentX *= lacunarity;
            currentZ *= lacunarity;
            currentAmplitude *= persistence;
        }

        // Normalize the output to [0,1] range
        return (total / amplitudeSum + 1) / 2;
    }
}