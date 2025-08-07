package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:z_clamped_gradient")
public class ZClampedGradientDensityFunction extends DensityFunction {
    private final DensityFunction from;
    private final DensityFunction to;
    private final DensityFunction fromValue;
    private final DensityFunction toValue;

    public ZClampedGradientDensityFunction(Value[] args, long i) {
        super(args, i);
        this.from = ServerWorldDataService.getGenerator(args[0], i);
        this.to = ServerWorldDataService.getGenerator(args[1], i);
        this.fromValue = ServerWorldDataService.getGenerator(args[2], i);
        this.toValue = ServerWorldDataService.getGenerator(args[3], i);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        double from = this.from.evaluate(x, y, z);
        double to = this.to.evaluate(x, y, z);
        double fromVal = fromValue.evaluate(x, y, z);
        double toVal = toValue.evaluate(x, y, z);

        return from == to ? fromVal : fromVal + (Math.max(from, Math.min(z, to)) - from) / (to - from) * (toVal - fromVal);
    }
}