package omnivoxel.server.client.chunk.worldDataService.density;

public abstract class DensityFunction {
    public abstract String getFunctionID();

    public abstract float evaluate(float x, float y, float z);
}