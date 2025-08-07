package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:max")
public class MaxDensityFunction extends DensityFunction {
    private final DensityFunction arg1;
    private final DensityFunction arg2;

    public MaxDensityFunction(Value[] args, long i) {
        super(args, i);
        arg1 = ServerWorldDataService.getGenerator(args[0], i);
        arg2 = ServerWorldDataService.getGenerator(args[1], i);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return Math.max(arg1.evaluate(x, y, z), arg2.evaluate(x, y, z));
    }
}
