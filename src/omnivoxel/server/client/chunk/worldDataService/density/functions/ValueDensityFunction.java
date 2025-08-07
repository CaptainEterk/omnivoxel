package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:value")
public class ValueDensityFunction extends DensityFunction {
    private final double value;

    public ValueDensityFunction(Value[] args, long i) {
        super(args, i);
        value = Double.longBitsToDouble(i);
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return value;
    }
}