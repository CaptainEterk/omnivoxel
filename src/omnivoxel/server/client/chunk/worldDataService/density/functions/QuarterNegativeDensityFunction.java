package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.Function;
import omnivoxel.server.client.chunk.worldDataService.ServerWorldDataService;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.util.game.nodes.GameNode;
import omnivoxel.util.game.nodes.ObjectGameNode;

@Function(id = "omnivoxel:quarter_negative")
public class QuarterNegativeDensityFunction extends DensityFunction {
    private final DensityFunction arg;

    public QuarterNegativeDensityFunction(GameNode args, long seed) {
        super(args, seed);

        if (args instanceof ObjectGameNode objectGameNode) {
            this.arg = ServerWorldDataService.getDensityFunction(objectGameNode.object().get("arg"), seed);
        } else {
            throw new IllegalArgumentException("GameNode must be an ObjectGameNode, not " + args.getClass());
        }
    }

    @Override
    public double evaluate(double x, double y, double z) {
        double val = arg.evaluate(x, y, z);
        return val < 0.0f ? val * 0.25f : val;
    }
}
