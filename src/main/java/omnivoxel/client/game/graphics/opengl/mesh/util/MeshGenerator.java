package omnivoxel.client.game.graphics.opengl.mesh.util;

import omnivoxel.client.game.graphics.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.opengl.mesh.chunk.*;
import omnivoxel.client.game.graphics.opengl.mesh.definition.GeneralEntityMeshDefinition;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;
import org.joml.Matrix4f;

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

    public EntityMesh bufferizeEntityMesh(EntityMeshData mesh) {
        if (mesh.entity().getMesh() == null) {
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
            mesh.entity().getMeshData().setModel(new Matrix4f().translate(mesh.entity().getX(), mesh.entity().getY(), mesh.entity().getZ()).rotateY(mesh.entity().getYaw()).rotateX(mesh.entity().getX()));
            return entityMesh;
        }
        return mesh.entity().getMesh();
    }

    private int[] generateFloat(ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
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

        int stride = 5 * Float.BYTES;

        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 0L);

        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, stride, 3L * Float.BYTES);

        glBindVertexArray(0);

        return new int[]{vao, vbo, ebo};
    }

    private int[] generate(ByteBuffer vertexBuffer, ByteBuffer indexBuffer) {
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


        glVertexAttribIPointer(0, 1, GL_UNSIGNED_INT, 2 * Integer.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribIPointer(1, 1, GL_UNSIGNED_INT, 2 * Integer.BYTES, Integer.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        return new int[]{vao, vbo, ebo};
    }
}