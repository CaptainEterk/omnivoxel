package core.noise;

import omnivoxel.server.client.chunk.worldDataService.noise.Noise2D;

import java.util.Random;

public class ValueNoise implements Noise2D {
    private static final int GRID_SIZE = 256;
    private final double[][] grid;

    public ValueNoise(long seed) {
        grid = new double[GRID_SIZE][GRID_SIZE];
        Random random = new Random(seed);

        // Initialize random values for each grid point
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = random.nextDouble();
            }
        }
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    @Override
    public double generate(double x, double z) {
        int x0 = (int) Math.floor(x) % GRID_SIZE;
        int x1 = (x0 + 1) % GRID_SIZE;
        int z0 = (int) Math.floor(z) % GRID_SIZE;
        int z1 = (z0 + 1) % GRID_SIZE;

        double dx = x - Math.floor(x);
        double dz = z - Math.floor(z);

        double v00 = grid[x0][z0];
        double v01 = grid[x0][z1];
        double v10 = grid[x1][z0];
        double v11 = grid[x1][z1];

        double ix0 = lerp(dx, v00, v10);
        double ix1 = lerp(dx, v01, v11);
        return lerp(dz, ix0, ix1);
    }
}