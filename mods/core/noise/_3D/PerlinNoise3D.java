package core.noise._3D;

import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;

import java.util.Random;

public class PerlinNoise3D implements Noise3D {
    private final int[] permutations = new int[512];

    public PerlinNoise3D(long seed) {
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

    private static double grad(int hash, double x, double y, double z) {
        return switch (hash & 0xF) {
            case 0x0 -> x + y;
            case 0x1 -> -x + y;
            case 0x2 -> x - y;
            case 0x3 -> -x - y;
            case 0x4 -> x + z;
            case 0x5 -> -x + z;
            case 0x6 -> x - z;
            case 0x7 -> -x - z;
            case 0x8 -> y + z;
            case 0x9 -> -y + z;
            case 0xA -> y - z;
            case 0xB -> -y - z;
            case 0xC -> x + y;
            case 0xD -> -x + y;
            case 0xE -> x - y;
            case 0xF -> -x - y;
            default -> 0; // Should never happen
        };
    }

    @Override
    public double generate(double x, double y, double z) {
        int X = ((int) Math.floor(x)) & 255;
        int Y = ((int) Math.floor(y)) & 255;
        int Z = ((int) Math.floor(z)) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);

        double u = fade(x);
        double v = fade(y);
        double w = fade(z);

        int aaa = permutations[permutations[permutations[X] + Y] + Z];
        int aba = permutations[permutations[permutations[X] + Y + 1] + Z];
        int aab = permutations[permutations[permutations[X] + Y] + Z + 1];
        int abb = permutations[permutations[permutations[X] + Y + 1] + Z + 1];
        int baa = permutations[permutations[permutations[X + 1] + Y] + Z];
        int bba = permutations[permutations[permutations[X + 1] + Y + 1] + Z];
        int bab = permutations[permutations[permutations[X + 1] + Y] + Z + 1];
        int bbb = permutations[permutations[permutations[X + 1] + Y + 1] + Z + 1];

        return lerp(w,
                lerp(v,
                        lerp(u, grad(aaa, x, y, z), grad(baa, x - 1, y, z)),
                        lerp(u, grad(aba, x, y - 1, z), grad(bba, x - 1, y - 1, z))),
                lerp(v,
                        lerp(u, grad(aab, x, y, z - 1), grad(bab, x - 1, y, z - 1)),
                        lerp(u, grad(abb, x, y - 1, z - 1), grad(bbb, x - 1, y - 1, z - 1)))
        );
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }
}