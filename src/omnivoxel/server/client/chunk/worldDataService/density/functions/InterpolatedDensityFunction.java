package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:interpolated")
public class InterpolatedDensityFunction extends DensityFunction {
    private final DensityFunction argument;

    public InterpolatedDensityFunction(Value[] args, long i) {
        super(args, i);
        this.argument = ServerWorldDataService.getGenerator(args[0], i);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return argument.evaluate(x, y, z);
    }
}