package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:add")
public class AddDensityFunction extends DensityFunction {
    private final DensityFunction arg1;
    private final DensityFunction arg2;

    public AddDensityFunction(Value[] args, long i) {
        super(args, i);
        arg1 = ServerWorldDataService.getGenerator(args[0], i);
        arg2 = ServerWorldDataService.getGenerator(args[1], i);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return arg1.evaluate(x, y, z) + arg2.evaluate(x, y, z);
    }
}
