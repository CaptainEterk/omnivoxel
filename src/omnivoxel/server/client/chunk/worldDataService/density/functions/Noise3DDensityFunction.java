package omnivoxel.server.client.chunk.worldDataService.density.functions;

import omnivoxel.server.client.chunk.worldDataService.Function;
import omnivoxel.server.client.chunk.worldDataService.density.DensityFunction;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import omnivoxel.server.games.Game;
import omnivoxel.util.game.nodes.GameNode;
import omnivoxel.util.game.nodes.ObjectGameNode;
import omnivoxel.util.game.nodes.StringGameNode;

import java.util.HashMap;
import java.util.Map;

@Function(id = "omnivoxel:noise3d")
public class Noise3DDensityFunction extends DensityFunction {
    public final static Map<String, Noise3D> noises = new HashMap<>();
    private final Noise3D noise;

    public Noise3DDensityFunction(GameNode args, long seed) {
        super(args, seed);

        ObjectGameNode objectGameNode = Game.checkGameNodeType(args, ObjectGameNode.class);
        StringGameNode stringGameNode = Game.checkGameNodeType(objectGameNode.object().get("noise_id"), StringGameNode.class);
        this.noise = noises.get(stringGameNode.value());
    }

    @Override
    public double evaluate(double x, double y, double z) {
        return noise.generate(x, y, z);
    }
}