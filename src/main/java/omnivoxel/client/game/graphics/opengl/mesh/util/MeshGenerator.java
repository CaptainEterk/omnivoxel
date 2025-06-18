package omnivoxel.client.game.graphics.opengl.mesh.util;

import omnivoxel.client.game.graphics.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.opengl.mesh.chunk.*;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30C.*;

public class MeshGenerator {
    public ChunkMesh bufferizeChunkMesh(MeshData mesh) {
        int[] solid = generate(mesh.solidVertices(), mesh.solidIndices());
        int[] transparent = generate(mesh.transparentVertices(), mesh.transparentIndices());
        if (solid == null) {
            if (transparent == null) {
                return new EmptyChunkMesh();
            } else {
                return new TransparentChunkMesh(
                        transparent[0],
                        transparent[1],
                        transparent[2],
                        mesh.transparentIndices().capacity() / Integer.BYTES,
                        mesh
                );
            }
        } else if (transparent == null) {
            return new SolidChunkMesh(
                    solid[0],
                    solid[1],
                    solid[2],
                    mesh.solidIndices().capacity() / Integer.BYTES,
                    mesh
            );
        } else {
            return new GeneralChunkMesh(
                    solid[0],
                    solid[1],
                    solid[2],
                    mesh.solidIndices().capacity() / Integer.BYTES,
                    transparent[0],
                    transparent[1],
                    transparent[2],
                    mesh.transparentIndices().capacity() / Integer.BYTES,
                    mesh
            );
        }
    }

    public EntityMesh bufferizeEntityMesh(MeshData mesh) {
        int[] solid = generate(mesh.solidVertices(), mesh.solidIndices());
        int[] transparent = generate(mesh.transparentVertices(), mesh.transparentIndices());
        return new EntityMesh(
                solid[0],
                solid[1],
                solid[2],
                mesh.solidIndices().capacity() / Integer.BYTES,
                transparent[0],
                transparent[1],
                transparent[2],
                mesh.transparentIndices().capacity() / Integer.BYTES
        );
    }

    private int[] generate(ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
        if (vertexBuffer == null || indexBuffer == null) {
            return null;
        }

        // Generate and bind VAO
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Generate and bind VBO for vertex data
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Generate and bind EBO for index data
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        // Specify the layout of the vertex data
        // Assuming the vertex data is interleaved with data1 (uint) and data2 (uint)

        // Data1 attribute (1 uint)
        glVertexAttribIPointer(0, 1, GL_UNSIGNED_INT, 2 * Integer.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Data2 attribute (1 uint)
        glVertexAttribIPointer(1, 1, GL_UNSIGNED_INT, 2 * Integer.BYTES, Integer.BYTES);
        glEnableVertexAttribArray(1);

        // Unbind VBO and EBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        return new int[]{vao, vbo, ebo};
    }
}