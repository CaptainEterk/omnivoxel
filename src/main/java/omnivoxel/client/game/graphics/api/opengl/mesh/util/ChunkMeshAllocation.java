package omnivoxel.client.game.graphics.api.opengl.mesh.util;

public record ChunkMeshAllocation(
        long vertexOffset,
        int vertexSize,
        long indexOffset,
        int indexSize
) {
    private static final int VERTEX_SIZE = 12;
    private static final int INDEX_SIZE = 4;

    public int vertexCount() {
        return vertexSize / VERTEX_SIZE;
    }

    public int indexCount() {
        return indexSize / INDEX_SIZE;
    }

    /**
     * Value required by DrawElementsIndirectCommand.firstIndex.
     */
    public int firstIndex() {
        return Math.toIntExact(indexOffset / INDEX_SIZE);
    }

    /**
     * Value required by DrawElementsIndirectCommand.baseVertex.
     */
    public int baseVertex() {
        return Math.toIntExact(vertexOffset / VERTEX_SIZE);
    }
}