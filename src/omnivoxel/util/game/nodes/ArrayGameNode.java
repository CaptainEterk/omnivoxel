package omnivoxel.util.game.nodes;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record ArrayGameNode(String key, GameNode[] nodes) implements GameNode {
    @Override
    public @NotNull String toString() {
        return "ArrayGameNode{" +
                "key='" + key + '\'' +
                ", nodes=" + Arrays.deepToString(nodes) +
                '}';
    }
}