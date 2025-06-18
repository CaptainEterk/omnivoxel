package omnivoxel.client.game.graphics.opengl.mesh.generators;

import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.TextureVertex;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.UniqueVertex;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.Vertex;
import omnivoxel.client.game.graphics.opengl.shape.util.ShapeHelper;
import omnivoxel.server.client.entity.Entity;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityMeshDataGenerator {

    private void addPoint(List<Integer> vertices, List<Integer> indices, Map<UniqueVertex, Integer> vertexIndexMap, Vertex position, int tx, int ty, BlockFace normal, float r, float g, float b) {
        UniqueVertex vertex = new UniqueVertex(position, new TextureVertex(tx, ty), normal);

        if (!vertexIndexMap.containsKey(vertex)) {
            int[] vertexData = ShapeHelper.packVertexData(position, 0, r, g, b, normal, tx, ty);
            vertexIndexMap.put(vertex, vertices.size());
            for (int data : vertexData) {
                vertices.add(data);
            }
        }
        indices.add(vertexIndexMap.get(vertex) / 2);
    }

    private ByteBuffer createBuffer(List<Integer> data) {
        if (data.isEmpty()) {
            return null;
        }
        ByteBuffer buffer = MemoryUtil.memAlloc(data.size() * Integer.BYTES);
        try {
            for (int value : data) {
                buffer.putInt(value);
            }
            buffer.flip();
            return buffer;
        } catch (Exception e) {
            MemoryUtil.memFree(buffer);
            throw new RuntimeException("Error creating buffer", e);
        }
    }

    private MeshData generate(Entity entity) {
        List<Integer> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();

        float ex = entity.getX();
        float ey = entity.getY();
        float ez = entity.getZ();

        // Use unit cube centered around the entity position
        float[][] cubeOffsets = {
                {0, 0, ShapeHelper.PIXEL}, {ShapeHelper.PIXEL, 0, ShapeHelper.PIXEL}, {ShapeHelper.PIXEL, ShapeHelper.PIXEL, ShapeHelper.PIXEL}, {0, ShapeHelper.PIXEL, ShapeHelper.PIXEL}, // Front
                {ShapeHelper.PIXEL, 0, 0}, {0, 0, 0}, {0, ShapeHelper.PIXEL, 0}, {ShapeHelper.PIXEL, ShapeHelper.PIXEL, 0}, // Back
                {0, 0, 0}, {0, 0, ShapeHelper.PIXEL}, {0, ShapeHelper.PIXEL, ShapeHelper.PIXEL}, {0, ShapeHelper.PIXEL, 0}, // Left
                {ShapeHelper.PIXEL, 0, ShapeHelper.PIXEL}, {ShapeHelper.PIXEL, 0, 0}, {ShapeHelper.PIXEL, ShapeHelper.PIXEL, 0}, {ShapeHelper.PIXEL, ShapeHelper.PIXEL, ShapeHelper.PIXEL}, // Right
                {0, ShapeHelper.PIXEL, ShapeHelper.PIXEL}, {ShapeHelper.PIXEL, ShapeHelper.PIXEL, ShapeHelper.PIXEL}, {ShapeHelper.PIXEL, ShapeHelper.PIXEL, 0}, {0, ShapeHelper.PIXEL, 0}, // Top
                {0, 0, 0}, {ShapeHelper.PIXEL, 0, 0}, {ShapeHelper.PIXEL, 0, ShapeHelper.PIXEL}, {0, 0, ShapeHelper.PIXEL}  // Bottom
        };

        int[][] faceIndices = {
                {0, 1, 2, 2, 3, 0},     // Front
                {4, 5, 6, 6, 7, 4},     // Back
                {8, 9,10,10,11, 8},     // Left
                {12,13,14,14,15,12},    // Right
                {16,17,18,18,19,16},    // Top
                {20,21,22,22,23,20}     // Bottom
        };

        BlockFace[] faces = {
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.WEST, BlockFace.EAST,
                BlockFace.TOP, BlockFace.BOTTOM
        };

        for (int f = 0; f < 6; f++) {
            BlockFace face = faces[f];
            for (int i = 0; i < 6; i++) {
                int index = faceIndices[f][i];
                float[] offset = cubeOffsets[index];
                Vertex position = new Vertex(ex + offset[0], ey + offset[1], ez + offset[2]);

                // Basic texture mapping: 0 or 1
                int tx = (i % 3 == 0) ? 0 : 1;
                int ty = (i < 3) ? 0 : 1;

                // Color based on team/state (white for now)
                addPoint(vertices, indices, vertexIndexMap, position, tx, ty, face, 1.0f, 1.0f, 1.0f);
            }
        }

        ByteBuffer vertexBuffer = createBuffer(vertices);
        ByteBuffer indexBuffer = createBuffer(indices);

        return new EntityMeshData(vertexBuffer, indexBuffer, entity);
    }

    public Entity generateMeshData(Entity entity) {
        entity.setMeshData(generate(entity));
        return entity;
    }
}