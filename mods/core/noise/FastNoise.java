package core.noise;

import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;

public class FastNoise implements Noise2D {
    private final long seed;

    public FastNoise(long seed) {
        this.seed = seed;
    }

    private static long hash(int x, int z, long seed) {
        long h = seed ^ (x * 6364136223846793005L) ^ (z * 1442695040888963407L);
        h = (h ^ (h >> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >> 33)) * 0xc4ceb9fe1a85ec53L;
        return h ^ (h >> 33);
    }

    private static double grad(long hash, double x, double z) {
        int h = (int) (hash & 7); // Get lowest 3 bits
        double u = (h & 1) == 0 ? x : -x;
        double v = (h & 2) == 0 ? z : -z;
        return u + v;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10); // Smootherstep function
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static int fastFloor(double x) {
        return x >= 0 ? (int) x : (int) x - 1;
    }

    @Override
    public double generate(double x, double z) {
        int xi = fastFloor(x);
        int zi = fastFloor(z);

        double xf = x - xi;
        double zf = z - zi;

        double u = fade(xf);
        double v = fade(zf);

        long h00 = hash(xi, zi, seed);
        long h10 = hash(xi + 1, zi, seed);
        long h01 = hash(xi, zi + 1, seed);
        long h11 = hash(xi + 1, zi + 1, seed);

        double g00 = grad(h00, xf, zf);
        double g10 = grad(h10, xf - 1, zf);
        double g01 = grad(h01, xf, zf - 1);
        double g11 = grad(h11, xf - 1, zf - 1);

        double xLerp1 = lerp(u, g00, g10);
        double xLerp2 = lerp(u, g01, g11);

        return Math.max(0, Math.min(1, (lerp(v, xLerp1, xLerp2) + Math.sqrt(2)) / (2 * Math.sqrt(2))));
    }
}