package core.noise;

import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;

public class FractionalBrownianNoise implements Noise2D {
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
            double frequency
    ) {
        this.noise = noise;
        this.octaves = octaves;
        this.persistence = persistence;
        this.lacunarity = lacunarity;
        this.frequency = frequency;
    }

    public static void main(String[] args) {
        double target = 0.0;
        FractionalBrownianNoise fractionalBrownianNoise = new FractionalBrownianNoise(new PerlinNoise(106), 3, 0.25, 2.5, 0.001);
        for (int x = 0; true; x++) {
            double noiseValue = fractionalBrownianNoise.generate(x, 0);
            if (noiseValue > target) {
                System.out.println(x + ": " + noiseValue);
                if (target == 0.9) {
                    break;
                } else {
                    target += 0.1;
                }
            }
            if (x % 1000000 == 0) {
                System.out.println(x + ": " + noiseValue);
            }
        }
    }

    // Generate fBm noise at a given point (x, z)
    @Override
    public double generate(double x, double z) {
        double total = 0;
        double amplitude = 1;
        double maxValue = 0;
        double currentFrequency = frequency;

        for (int i = 0; i < octaves; i++) {
            total += noise.generate(x * currentFrequency, z * currentFrequency) * amplitude;

            maxValue += amplitude;
            amplitude *= persistence;   // Reduce amplitude for each octave
            currentFrequency *= lacunarity; // Increase frequency for each octave
        }

        // Normalize to 0 - 1
        return (total / maxValue + 1) / 2;
    }
}