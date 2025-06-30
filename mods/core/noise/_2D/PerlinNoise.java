package core.noise._2D;

import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;

import java.util.Random;

public class PerlinNoise implements Noise2D {
    private final int[] permutations = new int[512];

    public PerlinNoise(long seed) {
        Random random = new Random(seed);
        int[] p = new int[256];

        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }

        for (int i = 0; i < 512; i++) {
            permutations[i] = p[i & 255];
        }
    }

    private static int fastFloor(double x) {
        return x >= 0 ? (int) x : (int) x - 1;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double z) {
        int h = hash & 15;
        double u = (h < 8) ? x : z;
        double v = (h < 4) ? z : ((h == 12 || h == 14) ? x : 0);

        return ((h & 1) == 0 ? u : -u) +
                ((h & 2) == 0 ? v : -v);
    }

    @Override
    public double generate(double x, double z) {
        int X = fastFloor(x) & 255;
        int Z = fastFloor(z) & 255;

        double dx = x - fastFloor(x);
        double dz = z - fastFloor(z);

        double u = fade(dx);
        double v = fade(dz);

        int aa = permutations[permutations[X] + Z];
        int ab = permutations[permutations[X] + Z + 1];
        int ba = permutations[permutations[X + 1] + Z];
        int bb = permutations[permutations[X + 1] + Z + 1];

        double gradAA = grad(aa, dx, dz);
        double gradBA = grad(ba, dx - 1, dz);
        double gradAB = grad(ab, dx, dz - 1);
        double gradBB = grad(bb, dx - 1, dz - 1);

        double lerpX1 = lerp(u, gradAA, gradBA);
        double lerpX2 = lerp(u, gradAB, gradBB);

        return lerp(v, lerpX1, lerpX2);
    }
}