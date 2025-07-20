package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import org.graalvm.polyglot.Value;

public class Noise3DDensityFunction extends DensityFunction {
    private final Noise3D noise;

    public Noise3DDensityFunction(Value[] args) {
        noise = new Noise3D(args[0].as(String.class));
    }

    @Override
    public String getFunctionID() {
        return "omnivoxel:noise";
    }

    @Override
    public float evaluate(float x, float y, float z) {
        return noise.generate(x, y, z);
    }
}