package core.noise;

public class FastNoise3D {
    private final long seed;

    public FastNoise3D(long seed) {
        this.seed = seed;
    }

    private static long hash(int x, int y, int z, long seed) {
        long h = seed;
        h ^= (x * 6364136223846793005L) ^ (y * 1442695040888963407L) ^ (z * 2463534245172598565L);
        h = (h ^ (h >> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >> 33)) * 0xc4ceb9fe1a85ec53L;
        return h ^ (h >> 33);
    }

    private static double grad(long hash, double x, double y, double z) {
        int h = (int) (hash & 15); // Take lowest 4 bits
        double u = (h & 8) == 0 ? x : -x;
        double v = (h & 4) == 0 ? y : -y;
        double w = (h & 2) == 0 ? z : -z;
        return u + v + w;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10); // Smootherstep
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static int fastFloor(double x) {
        return x >= 0 ? (int) x : (int) x - 1;
    }

    public double generate(double x, double y, double z) {
        int xi = fastFloor(x);
        int yi = fastFloor(y);
        int zi = fastFloor(z);

        double xf = x - xi;
        double yf = y - yi;
        double zf = z - zi;

        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);

        long h000 = hash(xi, yi, zi, seed);
        long h100 = hash(xi + 1, yi, zi, seed);
        long h010 = hash(xi, yi + 1, zi, seed);
        long h110 = hash(xi + 1, yi + 1, zi, seed);
        long h001 = hash(xi, yi, zi + 1, seed);
        long h101 = hash(xi + 1, yi, zi + 1, seed);
        long h011 = hash(xi, yi + 1, zi + 1, seed);
        long h111 = hash(xi + 1, yi + 1, zi + 1, seed);

        double g000 = grad(h000, xf, yf, zf);
        double g100 = grad(h100, xf - 1, yf, zf);
        double g010 = grad(h010, xf, yf - 1, zf);
        double g110 = grad(h110, xf - 1, yf - 1, zf);
        double g001 = grad(h001, xf, yf, zf - 1);
        double g101 = grad(h101, xf - 1, yf, zf - 1);
        double g011 = grad(h011, xf, yf - 1, zf - 1);
        double g111 = grad(h111, xf - 1, yf - 1, zf - 1);

        double xLerp00 = lerp(u, g000, g100);
        double xLerp10 = lerp(u, g010, g110);
        double xLerp01 = lerp(u, g001, g101);
        double xLerp11 = lerp(u, g011, g111);

        double yLerp0 = lerp(v, xLerp00, xLerp10);
        double yLerp1 = lerp(v, xLerp01, xLerp11);

        return lerp(w, yLerp0, yLerp1); // Output is in the range [-1, 1]
    }
}