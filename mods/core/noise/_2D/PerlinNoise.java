package core.noise._2D;

import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;

import java.util.Random;

public class PerlinNoise implements Noise2D {
    private final int[] permutations = new int[512];

    public PerlinNoise(long seed) {
        Random random = new Random(seed);
        int[] p = new int[256];

        // Fill p with values from 0 to 255
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        // Shuffle the array
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }

        // Duplicate the array to avoid overflow
        for (int i = 0; i < 512; i++) {
            permutations[i] = p[i % 256];
        }
    }

    private static double grad(int hash, double x, double y) {
        return switch (hash & 0x7) {
            case 0x0 -> x + y;
            case 0x1 -> -x + y;
            case 0x2 -> x - y;
            case 0x3 -> -x - y;
            case 0x4 -> x;
            case 0x5 -> -x;
            case 0x6 -> y;
            case 0x7 -> -y;
            default -> 0; // never happens
        };
    }

    @Override
    public double generate(double x, double z) {
        int X = ((int) Math.floor(x)) & 255;
        int Z = ((int) Math.floor(z)) & 255;

        x -= Math.floor(x);
        z -= Math.floor(z);

        double u = fade(x);
        double v = fade(z);

        int aa = permutations[permutations[X] + Z];
        int ab = permutations[permutations[X] + Z + 1];
        int ba = permutations[permutations[X + 1] + Z];
        int bb = permutations[permutations[X + 1] + Z + 1];

        return lerp(v,
                lerp(u, grad(aa, x, z), grad(ba, x - 1, z)),
                lerp(u, grad(ab, x, z - 1), grad(bb, x - 1, z - 1))
        );
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }
}