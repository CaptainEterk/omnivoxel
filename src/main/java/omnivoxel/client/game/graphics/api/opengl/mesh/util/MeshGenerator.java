package omnivoxel.client.game.graphics.api.opengl.mesh.util;

import omnivoxel.client.game.graphics.api.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.RenderMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.chunk.EmptyChunkMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.chunk.GeneralChunkMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.definition.GeneralEntityMeshDefinition;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.ChunkMeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.util.math.Position3D;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL30C.*;

public class MeshGenerator {
    private final ChunkMeshBuffer chunkMeshBuffer;
    Map<Position3D, Integer> already = new HashMap<>();

    public MeshGenerator(ChunkMeshBuffer chunkMeshBuffer) {
        this.chunkMeshBuffer = chunkMeshBuffer;
    }

    public ChunkMesh bufferizeChunkMesh(ChunkMeshData mesh) {
        if (already.containsKey(mesh.chunkPosition())) {
            already.put(mesh.chunkPosition(), already.get(mesh.chunkPosition()) + 1);
        } else {
            already.put(mesh.chunkPosition(), 1);
        }

        RenderMesh solid = upload(
                mesh.solidVertices(),
                mesh.solidIndices()
        );

        RenderMesh transparent = upload(
                mesh.transparentVertices(),
                mesh.transparentIndices()
        );

        RenderMesh decoration = upload(
                mesh.decorationVertices(),
                mesh.decorationIndices()
        );

        if (solid.isEmpty()
                && transparent.isEmpty()
                && decoration.isEmpty()) {
            return new EmptyChunkMesh();
        }

        return new GeneralChunkMesh(
                solid,
                transparent,
                decoration,
                mesh.lod(),
                mesh
        );
    }

    private RenderMesh upload(
            ByteBuffer vertices,
            ByteBuffer indices
    ) {
        if (vertices == null
                || indices == null
                || !vertices.hasRemaining()
                || !indices.hasRemaining()) {
            return RenderMesh.EMPTY;
        }

        ChunkMeshAllocation allocation =
                chunkMeshBuffer.allocate(
                        vertices.remaining(),
                        indices.remaining()
                );

        chunkMeshBuffer.upload(
                allocation,
                vertices,
                indices
        );

        return new RenderMesh(
                allocation,
                allocation.indexCount()
        );
    }

    /*
     * Entity meshes still use their own VAO/VBO/EBO path.
     */
    public EntityMesh bufferizeEntityMesh(EntityMeshData mesh) {
        int[] solid =
                generateFloat(
                        mesh.solidVertices(),
                        mesh.solidIndices()
                );

        EntityMesh entityMesh = new EntityMesh(
                new GeneralEntityMeshDefinition(
                        solid[0],
                        solid[1],
                        solid[2],
                        mesh.solidIndices().remaining() / Integer.BYTES,
                        mesh
                ),
                mesh
        );

        for (EntityMeshData child : mesh.children()) {
            entityMesh.addChild(bufferizeEntityMesh(child));
        }

        return entityMesh;
    }

    private int[] generateFloat(
            ByteBuffer vertexBuffer,
            ByteBuffer indexBuffer
    ) {
        if (vertexBuffer == null
                || indexBuffer == null
                || !vertexBuffer.hasRemaining()
                || !indexBuffer.hasRemaining()) {
            return null;
        }

        int vao = glGenVertexArrays();

        glBindVertexArray(vao);

        int vbo = glGenBuffers();

        glBindBuffer(
                GL_ARRAY_BUFFER,
                vbo
        );

        glBufferData(
                GL_ARRAY_BUFFER,
                vertexBuffer,
                GL_STATIC_DRAW
        );

        int ebo = glGenBuffers();

        glBindBuffer(
                GL_ELEMENT_ARRAY_BUFFER,
                ebo
        );

        glBufferData(
                GL_ELEMENT_ARRAY_BUFFER,
                indexBuffer,
                GL_STATIC_DRAW
        );

        int stride = 5 * Float.BYTES;

        glEnableVertexAttribArray(3);

        glVertexAttribPointer(
                3,
                3,
                GL_FLOAT,
                false,
                stride,
                0L
        );

        glEnableVertexAttribArray(4);

        glVertexAttribPointer(
                4,
                2,
                GL_FLOAT,
                false,
                stride,
                3L * Float.BYTES
        );

        glBindVertexArray(0);

        return new int[]{vao, vbo, ebo};
    }

    public ChunkMeshBuffer getChunkMeshBuffer() {
        return chunkMeshBuffer;
    }
}