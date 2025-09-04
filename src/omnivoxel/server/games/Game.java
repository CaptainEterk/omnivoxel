package omnivoxel.server.games;

import omnivoxel.server.client.chunk.worldDataService.density.functions.Noise3DDensityFunction;
import omnivoxel.server.client.chunk.worldDataService.noise.Noise3D;
import omnivoxel.util.game.nodes.*;

import java.util.Arrays;

public final class Game {
    @SuppressWarnings("unchecked")
    public static <T extends GameNode> T checkGameNodeType(GameNode gameNode, Class<T> clazz) {
        if (gameNode == null) {
            return null;
        }
        if (clazz.isInstance(gameNode)) {
            return (T) gameNode;
        }
        throw new IllegalArgumentException("Expected " + clazz.getSimpleName() + " but got " + gameNode.getClass().getSimpleName());
    }

    public static void loadNoises(ArrayGameNode noises, long seed) {
        for (GameNode node : noises.nodes()) {
            loadNoise(checkGameNodeType(node, ObjectGameNode.class), seed);
        }
    }

    private static void loadNoise(ObjectGameNode noise, long seed) {
        String id = checkGameNodeType(noise.object().get("id"), StringGameNode.class).value();
        double[] octaves = Arrays.stream(checkGameNodeType(noise.object().get("octaves"), ArrayGameNode.class).nodes())
                .mapToDouble(gameNode -> checkGameNodeType(gameNode, DoubleGameNode.class).value())
                .toArray();
        double firstOctave = checkGameNodeType(noise.object().get("first_octave"), DoubleGameNode.class).value();
        Noise3DDensityFunction.noises.put(id, new Noise3D(octaves, firstOctave, seed));
    }
}