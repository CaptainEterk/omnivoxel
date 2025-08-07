package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.density.Function;
import org.graalvm.polyglot.Value;

@Function(id = "omnivoxel:x")
public class XDensityFunction extends DensityFunction {

    public XDensityFunction(Value[] args, long i) {
        super(args, i);
        // no args needed
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return x;
    }
}