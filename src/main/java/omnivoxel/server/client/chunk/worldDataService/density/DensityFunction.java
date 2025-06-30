package omnivoxel.server.client.chunk.worldDataService.density;

public abstract class DensityFunction {
    public abstract String getFunctionID();

    public abstract double evaluate(double x, double y, double z);
}