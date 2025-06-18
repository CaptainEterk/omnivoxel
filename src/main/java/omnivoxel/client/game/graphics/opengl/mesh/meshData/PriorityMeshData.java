package omnivoxel.client.game.graphics.opengl.mesh.meshData;

import omnivoxel.client.game.graphics.opengl.mesh.util.PriorityUtils;
import omnivoxel.math.Position3D;
import org.jetbrains.annotations.NotNull;

public record PriorityMeshData(Position3D position, MeshData meshData) implements Comparable<PriorityMeshData> {
    public Double getPriority() {
        return PriorityUtils.getPriority(position);
    }

    @Override
    public int compareTo(@NotNull PriorityMeshData priorityMeshData) {
        return Double.compare(getPriority(), priorityMeshData.getPriority());
    }

    @Override
    public String toString() {
        return "PriorityMeshData[" +
                "position=" + position + ", " +
                "meshData=" + meshData + ']';
    }
}