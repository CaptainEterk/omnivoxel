package core.noise;

import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;

import java.util.Random;

public class SimplexNoise implements Noise2D {
    private static final int PERM_SIZE = 512;
    private static final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
    private final int[] perm;

    public SimplexNoise(long seed) {
        perm = new int[PERM_SIZE];
        int[] p = new int[256];

        // Initialize permutation
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }

        // Shuffle with long seed
        Random random = new Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = (int) (random.nextLong() & 0xFF) % (i + 1);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }

        for (int i = 0; i < 256; i++) {
            perm[i] = perm[i + 256] = p[i];
        }
    }

    private static int fastFloor(double x) {
        return x >= 0 ? (int) x : (int) x - 1;
    }

    private static double grad(int hash, double x, double z) {
        int h = hash & 7;
        double u = h < 4 ? x : z;
        double v = h < 4 ? z : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    @Override
    public double generate(double x, double z) {
        double s = (x + z) * F2;
        int i = fastFloor(x + s);
        int j = fastFloor(z + s);

        double t = (i + j) * G2;
        double X0 = i - t;
        double Z0 = j - t;
        double x0 = x - X0;
        double z0 = z - Z0;

        int i1 = x0 > z0 ? 1 : 0;
        int j1 = x0 > z0 ? 0 : 1;

        double x1 = x0 - i1 + G2;
        double z1 = z0 - j1 + G2;
        double x2 = x0 - 1 + 2 * G2;
        double z2 = z0 - 1 + 2 * G2;

        int ii = i & 255;
        int jj = j & 255;

        double t0 = 0.5 - x0 * x0 - z0 * z0;
        double t1 = 0.5 - x1 * x1 - z1 * z1;
        double t2 = 0.5 - x2 * x2 - z2 * z2;

        double n0 = t0 < 0 ? 0 : (t0 * t0 * grad(perm[ii + perm[jj]], x0, z0));
        double n1 = t1 < 0 ? 0 : (t1 * t1 * grad(perm[ii + i1 + perm[jj + j1]], x1, z1));
        double n2 = t2 < 0 ? 0 : (t2 * t2 * grad(perm[ii + 1 + perm[jj + 1]], x2, z2));

        return (n0 + n1 + n2) * 70; // Scale to -1 to 1
    }
}