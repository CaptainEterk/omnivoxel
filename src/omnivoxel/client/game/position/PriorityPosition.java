package omnivoxel.client.game.position;

import omnivoxel.client.game.thread.mesh.util.PriorityUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PriorityPosition(ChunkPosition chunkPosition) implements Comparable<PriorityPosition> {
    public Double getPriority() {
        return PriorityUtils.getPriority(chunkPosition);
    }

    @Override
    public int compareTo(@NotNull PriorityPosition priorityPosition) {
        return Double.compare(getPriority(), priorityPosition.getPriority());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PriorityPosition) obj;
        return Objects.equals(this.chunkPosition, that.chunkPosition);
    }

    @Override
    public String toString() {
        return "PriorityPosition[" +
                "chunkPosition=" + chunkPosition + ']';
    }

}