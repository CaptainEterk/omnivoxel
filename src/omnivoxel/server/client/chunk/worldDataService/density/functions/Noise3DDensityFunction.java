package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;

@Function(id = "omnivoxel:noise3d")
public class Noise3DDensityFunction extends DensityFunction {
    public final static Map<String, Noise3D> noises = new HashMap<>();
    private final Noise3D noise;

    public Noise3DDensityFunction(Value[] args, long i) {
        super(args, i);
        noise = noises.get(args[0].asString());
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return noise.generate(x, y, z);
    }
}