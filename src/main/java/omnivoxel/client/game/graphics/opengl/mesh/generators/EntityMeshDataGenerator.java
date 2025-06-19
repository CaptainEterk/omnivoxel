package omnivoxel.client.game.graphics.opengl.mesh.generators;

import omnivoxel.client.game.entity.ClientEntity;
import omnivoxel.client.game.graphics.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.mesh.definition.EntityMeshDataDefinition;
import omnivoxel.client.game.graphics.opengl.mesh.definition.EntityMeshDataNoDefinition;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.TextureVertex;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.UniqueVertex;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.Vertex;
import omnivoxel.util.cache.IDCache;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;

public class EntityMeshDataGenerator {
    private final IDCache<String, EntityMeshDataDefinition> entityMeshDefinitionCache;
    private final Set<String> queuedEntityMeshData;

    public EntityMeshDataGenerator(IDCache<String, EntityMeshDataDefinition> entityMeshDefinitionCache, Set<String> queuedEntityMeshData) {
        this.entityMeshDefinitionCache = entityMeshDefinitionCache;
        this.queuedEntityMeshData = queuedEntityMeshData;
    }

    private void addPoint(
            List<Float> vertices,
            List<Integer> indices,
            Map<UniqueVertex, Integer> vertexIndexMap,
            Vertex position,
            float tx, float ty,
            BlockFace normal
    ) {
        UniqueVertex vertex = new UniqueVertex(position, new TextureVertex(tx, ty), normal);

        if (!vertexIndexMap.containsKey(vertex)) {
            int index = vertices.size() / 5;
            vertexIndexMap.put(vertex, index);

            vertices.add(position.px());
            vertices.add(position.py());
            vertices.add(position.pz());

            vertices.add(tx);
            vertices.add(ty);
        }

        indices.add(vertexIndexMap.get(vertex));
    }

    private ByteBuffer createFloatBuffer(List<Float> data) {
        if (data.isEmpty()) {
            return null;
        }
        ByteBuffer buffer = MemoryUtil.memAlloc(data.size() * Float.BYTES);
        try {
            for (float value : data) {
                buffer.putFloat(value);
            }
            buffer.flip();
            return buffer;
        } catch (Exception e) {
            MemoryUtil.memFree(buffer);
            throw new RuntimeException("Error creating buffer", e);
        }
    }

    private ByteBuffer createIntBuffer(List<Integer> data) {
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

    private EntityMeshData generate(ClientEntity entity) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();

        float ex = entity.getX();
        float ey = entity.getY();
        float ez = entity.getZ();

        float[][] cubeOffsets = {
                {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1}, // Front
                {1, -1, -1}, {-1, -1, -1}, {-1, 1, -1}, {1, 1, -1}, // Back
                {-1, -1, -1}, {-1, -1, 1}, {-1, 1, 1}, {-1, 1, -1}, // Left
                {1, -1, 1}, {1, -1, -1}, {1, 1, -1}, {1, 1, 1}, // Right
                {-1, 1, 1}, {1, 1, 1}, {1, 1, -1}, {-1, 1, -1}, // Top
                {-1, -1, -1}, {1, -1, -1}, {1, -1, 1}, {-1, -1, 1}  // Bottom
        };

        int[][] faceIndices = {
                {16, 17, 18, 18, 19, 16}, // Top
                {20, 21, 22, 22, 23, 20}, // Bottom
                {0, 1, 2, 2, 3, 0},       // North (Front)
                {4, 5, 6, 6, 7, 4},       // South (Back)
                {12, 13, 14, 14, 15, 12}, // East (Right)
                {8, 9, 10, 10, 11, 8}     // West (Left)
        };

        BlockFace[] faces = {
                BlockFace.TOP, BlockFace.BOTTOM,
                BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.EAST, BlockFace.WEST
        };

        for (int f = 0; f < 6; f++) {
            BlockFace face = faces[f];
            for (int i = 0; i < 6; i++) {
                int index = faceIndices[f][i];
                float[] offset = cubeOffsets[index];
                Vertex position = new Vertex(ex + offset[0], ey + offset[1], ez + offset[2]);

                int[][] uvMap = {
                        {0, 1}, {1, 1}, {1, 0}, {1, 0}, {0, 0}, {0, 1}
                };

                int[] uv = uvMap[i];
                int tx = face == BlockFace.SOUTH ? uv[0] : 0;
                int ty = face == BlockFace.SOUTH ? uv[1] : 0;

                addPoint(vertices, indices, vertexIndexMap, position, tx, ty, face);

            }
        }

        ByteBuffer vertexBuffer = createFloatBuffer(vertices);
        ByteBuffer indexBuffer = createIntBuffer(indices);

        return new EntityMeshData(vertexBuffer, indexBuffer, entity);
    }

    public ClientEntity generateMeshData(ClientEntity entity) {
        EntityMeshDataDefinition definition = entityMeshDefinitionCache.get(entity.getType().toString(), null);
        if (definition == null) {
            queuedEntityMeshData.add(entity.getType().toString());
            entity.setMeshData(generate(entity));
            entityMeshDefinitionCache.put(entity.getType().toString(), new EntityMeshDataNoDefinition(entity.getMeshData()));
            return entity;
        }
        entity.setMeshData(definition.meshData());
        entity.setMesh(new EntityMesh(definition));
        return entity;
    }
}