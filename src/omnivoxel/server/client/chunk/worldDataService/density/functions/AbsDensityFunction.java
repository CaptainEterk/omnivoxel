package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:abs")
public class AbsDensityFunction extends DensityFunction {
    private final DensityFunction arg;

    public AbsDensityFunction(Value[] args, long seed) {
        super(args, seed);

        arg = ServerWorldDataService.getGenerator(args[0], seed);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return Math.abs(arg.evaluate(x, y, z));
    }
}