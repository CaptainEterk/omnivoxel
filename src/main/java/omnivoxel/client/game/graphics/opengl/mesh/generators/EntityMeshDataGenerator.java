package omnivoxel.client.game.graphics.opengl.mesh.generators;

import omnivoxel.client.game.entity.ClientEntity;
import omnivoxel.client.game.graphics.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.opengl.mesh.block.face.BlockFace;
import omnivoxel.client.game.graphics.opengl.mesh.definition.EntityMeshDataDefinition;
import omnivoxel.client.game.graphics.opengl.mesh.definition.EntityMeshDataNoDefinition;
import omnivoxel.client.game.graphics.opengl.mesh.generators.meshShape.BoxMeshShape;
import omnivoxel.client.game.graphics.opengl.mesh.generators.meshShape.MeshShape;
import omnivoxel.client.game.graphics.opengl.mesh.generators.textureShape.BoxTextureShape;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.TextureVertex;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.UniqueVertex;
import omnivoxel.client.game.graphics.opengl.mesh.vertex.Vertex;
import omnivoxel.server.entity.EntityType;
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

    private EntityMeshData generate(ClientEntity entity, float width, float height, float length, float x, float y, float z) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();

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

        float[][][] uvMap = {
                {
                        {0.000f, 0.0f}, {0.125f, 0.0f}, {0.125f, 0.5f},
                        {0.125f, 0.5f}, {0.000f, 0.5f}, {0.000f, 0.0f}
                },
                {
                        {0.125f, 0.0f}, {0.250f, 0.0f}, {0.250f, 0.5f},
                        {0.250f, 0.5f}, {0.125f, 0.5f}, {0.125f, 0.0f}
                },
                {
                        {0.375f, 0.5f}, {0.250f, 0.5f}, {0.250f, 0.0f},
                        {0.250f, 0.0f}, {0.375f, 0.0f}, {0.375f, 0.5f}
                },
                {
                        {0.125f, 1.0f}, {0.000f, 1.0f}, {0.000f, 0.5f},
                        {0.000f, 0.5f}, {0.125f, 0.5f}, {0.125f, 1.0f}
                },
                {
                        {0.375f, 1.0f}, {0.250f, 1.0f}, {0.250f, 0.5f},
                        {0.250f, 0.5f}, {0.375f, 0.5f}, {0.375f, 1.0f}
                },
                {
                        {0.250f, 1.0f}, {0.125f, 1.0f}, {0.125f, 0.5f},
                        {0.125f, 0.5f}, {0.250f, 0.5f}, {0.250f, 1.0f}
                },
        };

        for (int f = 0; f < 6; f++) {
            BlockFace face = faces[f];
            for (int i = 0; i < 6; i++) {
                int index = faceIndices[f][i];
                float[] offset = cubeOffsets[index];
                Vertex position = new Vertex(offset[0] * width + x, offset[1] * height + y, offset[2] * length + z);

                float[] uv = uvMap[f][i];

                addPoint(vertices, indices, vertexIndexMap, position, uv[0], uv[1], face);
            }
        }

        ByteBuffer vertexBuffer = createFloatBuffer(vertices);
        ByteBuffer indexBuffer = createIntBuffer(indices);

        return new EntityMeshData(vertexBuffer, indexBuffer, entity, new ArrayList<>());
    }

    public EntityMeshData generate(ClientEntity entity, MeshShape[] meshShapes) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();

        for (MeshShape meshShape : meshShapes) {
            meshShape.generate(vertices, indices, vertexIndexMap);
        }

        ByteBuffer vertexBuffer = createFloatBuffer(vertices);
        ByteBuffer indexBuffer = createIntBuffer(indices);

        return new EntityMeshData(vertexBuffer, indexBuffer, entity, new ArrayList<>());
    }

    public ClientEntity generateMeshData(ClientEntity entity) {
        EntityMeshDataDefinition definition = entityMeshDefinitionCache.get(entity.getType().toString(), null);
        if (definition == null) {
            queuedEntityMeshData.add(entity.getType().toString());

            if (entity.getType().type() == EntityType.Type.PLAYER) {
                BoxTextureShape texture = new BoxTextureShape(256, 32);

                BoxTextureShape bodyTexture = texture.copy()
                        .setCoords(BlockFace.TOP, 48, 0, 16, 8)
                        .setCoords(BlockFace.BOTTOM, 64, 0, 16, 8)
                        .setCoords(BlockFace.NORTH, 64, 8, 16, 24)
                        .setCoords(BlockFace.SOUTH, 48, 8, 16, 24)
                        .setCoords(BlockFace.EAST, 80, 8, 8, 24)
                        .setCoords(BlockFace.WEST, 88, 8, 8, 24);

                BoxTextureShape headTexture = texture.copy()
                        .setCoords(BlockFace.TOP, 0, 16, 16, 16)
                        .setCoords(BlockFace.BOTTOM, 16, 16, 16, 16)
                        .setCoords(BlockFace.NORTH, 0, 0, 16, 16)
                        .setCoords(BlockFace.SOUTH, 32, 16, 16, 16)
                        .setCoords(BlockFace.EAST, 32, 0, -16, 16)
                        .setCoords(BlockFace.WEST, 32, 0, 16, 16);

                BoxTextureShape leftArmTexture = texture.copy()
                        .setCoords(BlockFace.TOP, 80, 0, 8, 8)
                        .setCoords(BlockFace.BOTTOM, 88, 0, 8, 8)
                        .setCoords(BlockFace.NORTH, 96, 8, 8, 24)
                        .setCoords(BlockFace.SOUTH, 104, 8, 8, 24)
                        .setCoords(BlockFace.EAST, 112, 8, 8, 24)
                        .setCoords(BlockFace.WEST, 120, 8, 8, 24);

                BoxTextureShape rightArmTexture = texture.copy()
                        .setCoords(BlockFace.TOP, 96, 0, 8, 8)
                        .setCoords(BlockFace.BOTTOM, 104, 0, 8, 8)
                        .setCoords(BlockFace.NORTH, 128, 8, 8, 24)
                        .setCoords(BlockFace.SOUTH, 136, 8, 8, 24)
                        .setCoords(BlockFace.EAST, 144, 8, 8, 24)
                        .setCoords(BlockFace.WEST, 152, 8, 8, 24);

                BoxTextureShape leftLegTexture = texture.copy()
                        .setCoords(BlockFace.TOP, 112, 0, 8, 8)
                        .setCoords(BlockFace.BOTTOM, 120, 0, 8, 8)
                        .setCoords(BlockFace.NORTH, 160, 8, 8, 24)
                        .setCoords(BlockFace.SOUTH, 168, 8, 8, 24)
                        .setCoords(BlockFace.EAST, 176, 8, 8, 24)
                        .setCoords(BlockFace.WEST, 184, 8, 8, 24);

                BoxTextureShape rightLegTexture = texture.copy()
                        .setCoords(BlockFace.TOP, 128, 0, 8, 8)
                        .setCoords(BlockFace.BOTTOM, 136, 0, 8, 8)
                        .setCoords(BlockFace.NORTH, 192, 8, 8, 24)
                        .setCoords(BlockFace.SOUTH, 200, 8, 8, 24)
                        .setCoords(BlockFace.EAST, 208, 8, 8, 24)
                        .setCoords(BlockFace.WEST, 216, 8, 8, 24);

                EntityMeshData body = generate(entity, new MeshShape[]{new BoxMeshShape(0, 0, 0, 1, 1.5f, 0.5f, bodyTexture)});

                EntityMeshData head = generate(entity, new MeshShape[]{new BoxMeshShape(0, 0.5f, 0, 1, 1, 1, headTexture)});

                EntityMeshData leftArm = generate(entity, new MeshShape[]{new BoxMeshShape(-0.25f, -0.75f, 0, 0.5f, 1.5f, 0.5f, leftArmTexture)});
                EntityMeshData rightArm = generate(entity, new MeshShape[]{new BoxMeshShape(0.25f, -0.75f, 0, 0.5f, 1.5f, 0.5f, rightArmTexture)});

                EntityMeshData leftLeg = generate(entity, new MeshShape[]{new BoxMeshShape(0, -0.75f, 0, 0.5f, 1.5f, 0.5f, leftLegTexture)});
                EntityMeshData rightLeg = generate(entity, new MeshShape[]{new BoxMeshShape(0, -0.75f, 0, 0.5f, 1.5f, 0.5f, rightLegTexture)});

                body.addChild(head);
                body.addChild(leftArm);
                body.addChild(rightArm);
                body.addChild(leftLeg);
                body.addChild(rightLeg);

                entity.setMeshData(body);
            }

            entityMeshDefinitionCache.put(entity.getType().toString(), new EntityMeshDataNoDefinition(entity.getMeshData()));
            return entity;
        }
        entity.setMeshData(definition.meshData());
        entity.setMesh(new EntityMesh(definition));
        return entity;
    }
}