package omnivoxel.server.client.chunk.worldDataService.noise;

import java.util.Random;

public class MinecraftPerlinNoise {
    private final int[] permutations;
    private static final int PERM_SIZE = 512;

    public MinecraftPerlinNoise(long seed) {
        this.permutations = new int[PERM_SIZE];
        Random random = new Random(seed);

        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        // shuffle
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256 - i) + i;
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }

        // duplicate into perms
        for (int i = 0; i < PERM_SIZE; i++) {
            permutations[i] = p[i & 255];
        }
    }

    private static double fade(double t) {
        // 6t^5 - 15t^4 + 10t^3
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) +
               ((h & 2) == 0 ? v : -v);
    }

    public double sample(double x, double y, double z) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        int Z = (int) Math.floor(z) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        int A  = permutations[X] + Y;
        int AA = permutations[A] + Z;
        int AB = permutations[A + 1] + Z;
        int B  = permutations[X + 1] + Y;
        int BA = permutations[B] + Z;
        int BB = permutations[B + 1] + Z;

        return lerp(w,
                lerp(v,
                        lerp(u, grad(permutations[AA], x, y, z),
                                grad(permutations[BA], x - 1, y, z)),
                        lerp(u, grad(permutations[AB], x, y - 1, z),
                                grad(permutations[BB], x - 1, y - 1, z))),
                lerp(v,
                        lerp(u, grad(permutations[AA + 1], x, y, z - 1),
                                grad(permutations[BA + 1], x - 1, y, z - 1)),
                        lerp(u, grad(permutations[AB + 1], x, y - 1, z - 1),
                                grad(permutations[BB + 1], x - 1, y - 1, z - 1)))
        );
    }
}
