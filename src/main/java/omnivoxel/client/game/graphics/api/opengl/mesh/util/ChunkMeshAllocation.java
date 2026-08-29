package omnivoxel.client.game.graphics.api.opengl.mesh.util;

public final class ChunkMeshAllocation {
    private static final int VERTEX_SIZE = 12;
    private static final int INDEX_SIZE = 4;

    private long vertexOffset;
    private final int vertexSize;

    private long indexOffset;
    private final int indexSize;

    public ChunkMeshAllocation(
            long vertexOffset,
            int vertexSize,
            long indexOffset,
            int indexSize
    ) {
        if (vertexOffset < 0) {
            throw new IllegalArgumentException(
                    "Vertex offset cannot be negative"
            );
        }

        if (indexOffset < 0) {
            throw new IllegalArgumentException(
                    "Index offset cannot be negative"
            );
        }

        if (vertexSize < 0) {
            throw new IllegalArgumentException(
                    "Vertex size cannot be negative"
            );
        }

        if (indexSize < 0) {
            throw new IllegalArgumentException(
                    "Index size cannot be negative"
            );
        }

        this.vertexOffset = vertexOffset;
        this.vertexSize = vertexSize;
        this.indexOffset = indexOffset;
        this.indexSize = indexSize;
    }

    public long vertexOffset() {
        return vertexOffset;
    }

    public int vertexSize() {
        return vertexSize;
    }

    public long indexOffset() {
        return indexOffset;
    }

    public int indexSize() {
        return indexSize;
    }

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

    /*
     * Used exclusively by ChunkMeshBuffer during compaction.
     */
    void setVertexOffset(long vertexOffset) {
        if (vertexOffset < 0) {
            throw new IllegalArgumentException(
                    "Vertex offset cannot be negative"
            );
        }

        this.vertexOffset = vertexOffset;
    }

    /*
     * Used exclusively by ChunkMeshBuffer during compaction.
     */
    void setIndexOffset(long indexOffset) {
        if (indexOffset < 0) {
            throw new IllegalArgumentException(
                    "Index offset cannot be negative"
            );
        }

        this.indexOffset = indexOffset;
    }

    @Override
    public String toString() {
        return "ChunkMeshAllocation[" +
                "vertexOffset=" + vertexOffset +
                ", vertexSize=" + vertexSize +
                ", indexOffset=" + indexOffset +
                ", indexSize=" + indexSize +
                ']';
    }
}