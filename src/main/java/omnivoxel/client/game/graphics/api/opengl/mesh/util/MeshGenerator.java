package omnivoxel.client.game.graphics.api.opengl.mesh.util;

import omnivoxel.client.game.graphics.api.opengl.OpenGLChecks;
import omnivoxel.client.game.graphics.api.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.chunk.ChunkMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.chunk.EmptyChunkMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.chunk.GeneralChunkMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.definition.GeneralEntityMeshDefinition;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.ChunkMeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.EntityMeshData;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30C.*;

public class MeshGenerator {
    public ChunkMesh bufferizeChunkMesh(ChunkMeshData mesh) {
        int[] solid = generateInt(mesh.solidVertices(), mesh.solidIndices());
        int[] transparent = generateInt(mesh.transparentVertices(), mesh.transparentIndices());
        int[] decoration = generateInt(mesh.decorationVertices(), mesh.decorationIndices());
        if (solid == null && transparent == null && decoration == null) {
            return new EmptyChunkMesh();
        } else {
            return new GeneralChunkMesh(
                    solid == null ? 0 : solid[0],
                    solid == null ? 0 : solid[1],
                    solid == null ? 0 : solid[2],
                    solid == null ? 0 : mesh.solidIndices().capacity() / Integer.BYTES,
                    transparent == null ? 0 : transparent[0],
                    transparent == null ? 0 : transparent[1],
                    transparent == null ? 0 : transparent[2],
                    transparent == null ? 0 : mesh.transparentIndices().capacity() / Integer.BYTES,
                    decoration == null ? 0 : decoration[0],
                    decoration == null ? 0 : decoration[1],
                    decoration == null ? 0 : decoration[2],
                    decoration == null ? 0 : mesh.decorationIndices().capacity() / Integer.BYTES,
                    mesh
            );
        }
    }

    public EntityMesh bufferizeEntityMesh(EntityMeshData mesh) {
        int[] solid = generateFloat(mesh.solidVertices(), mesh.solidIndices());
        EntityMesh entityMesh = new EntityMesh(
                new GeneralEntityMeshDefinition(
                        solid[0],
                        solid[1],
                        solid[2],
                        mesh.solidIndices().capacity() / Integer.BYTES,
                        mesh
                ),
                mesh
        );
        mesh.children().forEach(entityMeshData -> {
            EntityMesh em = bufferizeEntityMesh(entityMeshData);
            entityMesh.addChild(em);
        });
        return entityMesh;
    }

    private int[] generateFloat(ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
        if (vertexBuffer == null || indexBuffer == null || vertexBuffer.capacity() == 0 || indexBuffer.capacity() == 0) {
            return null;
        }

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        int stride = 5 * Float.BYTES;

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 3, GL_FLOAT, false, stride, 0L);

        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 2, GL_FLOAT, false, stride, 3L * Float.BYTES);

        glBindVertexArray(0);
        OpenGLChecks.checkError("bufferize entity mesh");

        return new int[]{vao, vbo, ebo};
    }

    private int[] generateInt(ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
        if (vertexBuffer == null || indexBuffer == null) {
            return null;
        }

        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribIPointer(0, 1, GL_UNSIGNED_INT, 3 * Integer.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribIPointer(1, 1, GL_UNSIGNED_INT, 3 * Integer.BYTES, Integer.BYTES);

        glEnableVertexAttribArray(2);
        glVertexAttribIPointer(2, 1, GL_UNSIGNED_INT, 3 * Integer.BYTES, 2 * Integer.BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        OpenGLChecks.checkError("bufferize chunk mesh");

        return new int[]{vao, vbo, ebo};
    }
}
