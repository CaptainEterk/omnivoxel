package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:range_choice")
public class RangeChoiceDensityFunction extends DensityFunction {
    private final DensityFunction input;
    private final DensityFunction minInclusive;
    private final DensityFunction maxExclusive;
    private final DensityFunction whenInRange;
    private final DensityFunction whenOutOfRange;

    public RangeChoiceDensityFunction(Value[] args, long seed) {
        super(args, seed);

        this.input = ServerWorldDataService.getGenerator(args[0], seed);
        this.minInclusive = ServerWorldDataService.getGenerator(args[1], seed);
        this.maxExclusive = ServerWorldDataService.getGenerator(args[2], seed);
        this.whenInRange = ServerWorldDataService.getGenerator(args[3], seed);
        this.whenOutOfRange = ServerWorldDataService.getGenerator(args[4], seed);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        double v = input.evaluate(x, y, z);
        if (v >= minInclusive.evaluate(x, y, z) && v < maxExclusive.evaluate(x, y, z)) {
            return whenInRange.evaluate(x, y, z);
        }
        return whenOutOfRange.evaluate(x, y, z);
    }
}