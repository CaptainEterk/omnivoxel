package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:quarter_negative")
public class QuarterNegativeDensityFunction extends DensityFunction {
    private final DensityFunction argument;

    public QuarterNegativeDensityFunction(Value[] args, long i) {
        super(args, i);
        this.argument = ServerWorldDataService.getGenerator(args[0], i);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        double val = argument.evaluate(x, y, z);
        return val < 0.0f ? val * 0.25f : val;
    }
}
