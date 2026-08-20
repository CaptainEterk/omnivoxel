package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.Function;
import omnivoxel.server.client.chunk.worldDataService.WorldGenerator;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.util.game.nodes.GameNode;
import omnivoxel.util.game.nodes.ObjectGameNode;

@Function(id = "squeeze")
public class SqueezeDensityFunction extends DensityFunction {
    private final DensityFunction arg;

    public SqueezeDensityFunction(GameNode args, long seed) {
        super(args, seed);

        if (args instanceof ObjectGameNode objectGameNode) {
            this.arg = WorldGenerator.getDensityFunction(objectGameNode.object().get("arg"), seed);
        } else {
            throw new IllegalArgumentException("GameNode must be an ObjectGameNode, not " + args.getClass());
        }
    }

    @Override
    public double evaluate(double x, double y, double z) {
        double s = Math.clamp(arg.evaluate(x, y, z), -1.0f, 1.0f);
        return (s / 2.0f) - (s * s * s) / 24.0f;
    }
}