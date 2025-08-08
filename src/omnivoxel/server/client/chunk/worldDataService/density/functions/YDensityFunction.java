package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:y")
public class YDensityFunction extends DensityFunction {
    public YDensityFunction(Value[] args, long seed) {
        super(args, seed);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return y;
    }
}
