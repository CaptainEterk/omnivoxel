package omnivoxel.server.client.chunk.worldDataService.noise;

import java.util.Random;

public class Noise3D {
    private final PerlinNoise[] octaveNoises;
    private final double[] amplitudes;
    private final double[] frequencies;

    public Noise3D(double[] amplitudes, double firstOctave, long seed) {
        this.amplitudes = amplitudes.clone();

        int octaveCount = amplitudes.length;

        this.octaveNoises = new PerlinNoise[octaveCount];
        this.frequencies = new double[octaveCount];

        Random random = new Random(seed);

        double frequency = Math.pow(2.0, firstOctave);

        for (int i = 0; i < octaveCount; i++) {
            frequencies[i] = frequency;

            if (amplitudes[i] != 0.0) {
                octaveNoises[i] =
                        new PerlinNoise(random.nextLong());
            }

            frequency *= 2.0;
        }
    }

    public double generate(double x, double y, double z) {
        double result = 0.0;

        for (int i = 0; i < octaveNoises.length; i++) {
            PerlinNoise noise = octaveNoises[i];

            if (noise != null) {
                double frequency = frequencies[i];

                result += amplitudes[i] * noise.sample(
                        x * frequency,
                        y * frequency,
                        z * frequency
                );
            }
        }

        return result;
    }
}