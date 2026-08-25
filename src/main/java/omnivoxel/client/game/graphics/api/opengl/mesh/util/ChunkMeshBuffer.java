package omnivoxel.client.game.graphics.api.opengl.mesh.util;

import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;

public class ChunkMeshBuffer {
    private static final int VERTEX_STRIDE = 3 * Integer.BYTES;
    int count = 0;
    private int vertexBuffer;
    private int indexBuffer;
    private int vao;
    private long vertexCapacity;
    private long indexCapacity;
    private long vertexOffset;
    private long indexOffset;

    public int vao() {
        return vao;
    }

    public int vertexBuffer() {
        return vertexBuffer;
    }

    public int indexBuffer() {
        return indexBuffer;
    }

    public long vertexCapacity() {
        return vertexCapacity;
    }

    public long indexCapacity() {
        return indexCapacity;
    }

    public long vertexOffset() {
        return vertexOffset;
    }

    public long indexOffset() {
        return indexOffset;
    }

    public void init(long vertexCapacity, long indexCapacity) {
        if (vertexCapacity <= 0 || indexCapacity <= 0) {
            throw new IllegalArgumentException(
                    "Chunk mesh buffer capacities must be greater than zero"
            );
        }

        this.vertexCapacity = vertexCapacity;
        this.indexCapacity = indexCapacity;

        vertexOffset = 0;
        indexOffset = 0;

        vertexBuffer = GL45C.glCreateBuffers();
        indexBuffer = GL45C.glCreateBuffers();
        vao = GL45C.glCreateVertexArrays();

        GL45C.glNamedBufferStorage(
                vertexBuffer,
                vertexCapacity,
                GL45C.GL_DYNAMIC_STORAGE_BIT
        );

        GL45C.glNamedBufferStorage(
                indexBuffer,
                indexCapacity,
                GL45C.GL_DYNAMIC_STORAGE_BIT
        );

        /*
         * Vertex format:
         *
         * uint position;
         * uint data;
         * uint normal;
         */

        GL45C.glVertexArrayVertexBuffer(
                vao,
                0,
                vertexBuffer,
                0,
                VERTEX_STRIDE
        );

        GL45C.glVertexArrayElementBuffer(
                vao,
                indexBuffer
        );

        GL45C.glEnableVertexArrayAttrib(vao, 0);
        GL45C.glVertexArrayAttribIFormat(
                vao,
                0,
                1,
                GL45C.GL_UNSIGNED_INT,
                0
        );
        GL45C.glVertexArrayAttribBinding(vao, 0, 0);

        GL45C.glEnableVertexArrayAttrib(vao, 1);
        GL45C.glVertexArrayAttribIFormat(
                vao,
                1,
                1,
                GL45C.GL_UNSIGNED_INT,
                Integer.BYTES
        );
        GL45C.glVertexArrayAttribBinding(vao, 1, 0);

        GL45C.glEnableVertexArrayAttrib(vao, 2);
        GL45C.glVertexArrayAttribIFormat(
                vao,
                2,
                1,
                GL45C.GL_UNSIGNED_INT,
                2 * Integer.BYTES
        );
        GL45C.glVertexArrayAttribBinding(vao, 2, 0);
    }

    public ChunkMeshAllocation allocate(int vertexSize, int indexSize) {
        System.out.println(count++);

        if (vertexOffset + vertexSize > vertexCapacity) {
            throw new IllegalStateException(
                    "Chunk vertex buffer is full: requested=" + vertexSize +
                            ", remaining=" + (vertexCapacity - vertexOffset) +
                            ", capacity=" + vertexCapacity
            );
        }

        if (indexOffset + indexSize > indexCapacity) {
            throw new IllegalStateException(
                    "Chunk index buffer is full"
            );
        }
        long allocatedVertexOffset = vertexOffset;
        long allocatedIndexOffset = indexOffset;

        vertexOffset += vertexSize;
        indexOffset += indexSize;

        System.out.println("empty size: " + (vertexCapacity - vertexOffset));

        return new ChunkMeshAllocation(
                allocatedVertexOffset,
                vertexSize,
                allocatedIndexOffset,
                indexSize
        );
    }

    public void upload(
            ChunkMeshAllocation allocation,
            ByteBuffer vertices,
            ByteBuffer indices
    ) {
        if (vertices.remaining() != allocation.vertexSize()) {
            throw new IllegalArgumentException(
                    "Vertex data size does not match allocation"
            );
        }

        if (indices.remaining() != allocation.indexSize()) {
            throw new IllegalArgumentException(
                    "Index data size does not match allocation"
            );
        }

        GL45C.glNamedBufferSubData(
                vertexBuffer,
                allocation.vertexOffset(),
                vertices
        );

        GL45C.glNamedBufferSubData(
                indexBuffer,
                allocation.indexOffset(),
                indices
        );
    }

    /*
     * Clears the allocation arena.
     *
     * Existing ChunkMeshAllocation objects become invalid.
     * The OpenGL buffers themselves are retained.
     */
    public void reset() {
        vertexOffset = 0;
        indexOffset = 0;
    }

    public long remainingVertexBytes() {
        return vertexCapacity - vertexOffset;
    }

    public long remainingIndexBytes() {
        return indexCapacity - indexOffset;
    }

    public void cleanup() {
        if (vao != 0) {
            GL45C.glDeleteVertexArrays(vao);
            vao = 0;
        }

        if (vertexBuffer != 0) {
            GL45C.glDeleteBuffers(vertexBuffer);
            vertexBuffer = 0;
        }

        if (indexBuffer != 0) {
            GL45C.glDeleteBuffers(indexBuffer);
            indexBuffer = 0;
        }

        vertexCapacity = 0;
        indexCapacity = 0;
        vertexOffset = 0;
        indexOffset = 0;
    }
}