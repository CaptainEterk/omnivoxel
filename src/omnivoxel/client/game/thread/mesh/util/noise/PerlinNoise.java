package omnivoxel.client.game.thread.mesh.util.noise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PerlinNoise {
    private final int[] permutations;  // Permutation array for noise
    private final Random random;

    public PerlinNoise(long seed) {
        this.random = new Random(seed);
        this.permutations = initPermutations();
    }

    /**
     * Initializes the permutation array that generates the noise pattern, based on the global seed.
     *
     * @return An array of 512 integers. Not sure what their properties are.
     */
    private int[] initPermutations() {
        List<Integer> permutations = new ArrayList<>();
        for (int i = 0; i <= 255; i++) {
            permutations.add(random.nextInt(i + 1), i);
        }
        int[] out = new int[512];
        for (int i = 0; i < 512; i++) {
            out[i] = permutations.get(i % 256);
        }
        return out;
    }

    /**
     * Generates a <code>double</code> between 0 and 1, to be scaled to the user's needs.
     *
     * @param x   The X coordinate in the perlin noise
     * @param z   The Y coordinate in the perlin noise
     * @param res A number between 0 and 1 that creates spikier noise maps
     * @return A <code>double</code> between 0 and 1
     */
    public double perlin2d(long x, long z, double res) {
        // Use Perlin-like noise for height generation
        return noise(permutations, x * res, z * res);
    }

    // Fade function for smoothing the noise
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    // Linear interpolation
    private double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    // Gradient function
    private double grad(int hash, double x, double y) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : 0.0f;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    // Noise generation function
    private double noise(int[] p, double x, double y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);

        double u = fade(x);
        double v = fade(y);

        int aa = p[X] + Y;
        int ab = p[X] + Y + 1;
        int ba = p[X + 1] + Y;
        int bb = p[X + 1] + Y + 1;

        return lerp(v, lerp(u, grad(p[aa], x, y), grad(p[ba], x - 1, y)),
                lerp(u, grad(p[ab], x, y - 1), grad(p[bb], x - 1, y - 1)));
    }
}