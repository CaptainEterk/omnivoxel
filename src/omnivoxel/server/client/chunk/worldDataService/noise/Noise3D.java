package omnivoxel.server.client.chunk.worldDataService.noise;

import java.util.Random;

public class Noise3D {
    private static final int CACHE_SIZE = 3;
    private final PerlinNoise[] octaveNoises;
    private final double[] amplitudes;
    private final int firstOctave;

    private final double[] cachedX = new double[CACHE_SIZE];
    private final double[] cachedY = new double[CACHE_SIZE];
    private final double[] cachedZ = new double[CACHE_SIZE];
    private final double[] cachedV = new double[CACHE_SIZE];
    private final long[] cachedTime = new long[CACHE_SIZE];

    public Noise3D(double[] amplitudes, double firstOctave, long seed) {
        this.amplitudes = amplitudes;
        this.firstOctave = (int) firstOctave;
        this.octaveNoises = new PerlinNoise[amplitudes.length];

        Random random = new Random(seed);

        for (int i = 0; i < amplitudes.length; i++) {
            if (amplitudes[i] != 0.0f) {
                this.octaveNoises[i] = new PerlinNoise(random.nextLong());
            }
        }
    }

    public double generate(double x, double y, double z) {
//        for (int i = 0; i < CACHE_SIZE; i++) {
//            if (cachedX[i] == x && cachedY[i] == y && cachedZ[i] == z) {
//                return cachedV[i];
//            }
//        }

        double result = 0.0;
        double frequency = Math.pow(2.0, firstOctave);

        for (int i = 0; i < amplitudes.length; i++) {
            PerlinNoise perlin = octaveNoises[i];
            if (perlin != null) {
                result += amplitudes[i] * perlin.sample(
                        x * frequency,
                        y * frequency,
                        z * frequency
                );
            }
            frequency *= 2.0;
        }

//        long lowCachedTime = Long.MAX_VALUE;
//        int idx = 0;
//        for (int i = 0; i < CACHE_SIZE; i++) {
//            if (cachedTime[i] < lowCachedTime) {
//                lowCachedTime = cachedTime[i];
//                idx = i;
//            }
//        }

//        cachedX[idx] = x;
//        cachedY[idx] = y;
//        cachedZ[idx] = z;
//        cachedV[idx] = result;
//        cachedTime[idx] = System.nanoTime();

        return result;
    }
}