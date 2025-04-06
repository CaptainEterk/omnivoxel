package omnivoxel.server.client.chunk.worldDataService.noise;

public interface Noise2D {
    /**
     * Generates a pseudo-random value 0-1
     * @param x The position on the X-Axis
     * @param z The position on the Z-Axis
     * @return A double between 0-1.
     */
    double generate(double x, double z);
}