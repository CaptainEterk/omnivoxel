package omnivoxel.client.game.graphics.api.opengl.mesh;

import omnivoxel.client.game.graphics.api.opengl.mesh.util.ChunkMeshAllocation;

public record RenderMesh(
        ChunkMeshAllocation allocation,
        int indexCount
) {
    public static final RenderMesh EMPTY =
            new RenderMesh(new ChunkMeshAllocation(0, 0, 0, 0), 0);

    public boolean isEmpty() {
        return allocation == null || indexCount <= 0;
    }

    public long indexOffset() {
        return allocation.indexOffset();
    }

    public long vertexOffset() {
        return allocation.vertexOffset();
    }

    public int firstIndex() {
        return allocation.firstIndex();
    }

    public int baseVertex() {
        return allocation.baseVertex();
    }
}