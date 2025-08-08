package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:squeeze")
public class SqueezeDensityFunction extends DensityFunction {
    private final DensityFunction argument;

    public SqueezeDensityFunction(Value[] args, long seed) {
        super(args, seed);
        this.argument = ServerWorldDataService.getGenerator(args[0], seed);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        double s = Math.max(-1.0f, Math.min(1.0f, argument.evaluate(x, y, z)));
        return (s / 2.0f) - (s * s * s) / 24.0f;
    }
}