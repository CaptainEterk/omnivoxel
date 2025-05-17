package omnivoxel.client.game.position;

import omnivoxel.client.game.thread.mesh.util.PriorityUtils;
import omnivoxel.math.Position3D;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record PriorityPosition(Position3D position3D) implements Comparable<PriorityPosition> {
    public Double getPriority() {
        return PriorityUtils.getPriority(position3D);
    }

    @Override
    public int compareTo(@NotNull PriorityPosition priorityPosition) {
        return Double.compare(getPriority(), priorityPosition.getPriority());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        PriorityPosition that = (PriorityPosition) obj;
        return Objects.equals(this.position3D, that.position3D);
    }

    @Override
    public String toString() {
        return "PriorityPosition[" +
                "chunkPosition=" + position3D + ']';
    }

}