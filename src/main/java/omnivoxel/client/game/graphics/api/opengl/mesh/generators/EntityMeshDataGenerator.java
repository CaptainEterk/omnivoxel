package omnivoxel.client.game.graphics.api.opengl.mesh.generators;

import omnivoxel.client.game.entity.EntityMeshWrapper;
import omnivoxel.client.game.graphics.api.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.definition.EntityMeshDataDefinition;
import omnivoxel.client.game.graphics.api.opengl.mesh.definition.EntityMeshDataNoDefinition;
import omnivoxel.client.game.graphics.api.opengl.mesh.generators.meshShape.BoxMeshShape;
import omnivoxel.client.game.graphics.api.opengl.mesh.generators.meshShape.MeshShape;
import omnivoxel.client.game.graphics.api.opengl.mesh.generators.textureShape.BoxTextureShape;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.GeneralEntityMeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.ModelEntityMeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.vertex.UniqueVertex;
import omnivoxel.common.face.BlockFace;
import omnivoxel.server.entity.EntityType;
import omnivoxel.util.cache.IDCache;
import omnivoxel.util.log.Logger;

import java.nio.ByteBuffer;
import java.util.*;

public class EntityMeshDataGenerator {
    private final IDCache<EntityType, EntityMeshDataDefinition> entityMeshDefinitionCache;
    private final Set<EntityType> queuedEntityMeshData;

    public EntityMeshDataGenerator(IDCache<EntityType, EntityMeshDataDefinition> entityMeshDefinitionCache, Set<EntityType> queuedEntityMeshData) {
        this.entityMeshDefinitionCache = entityMeshDefinitionCache;
        this.queuedEntityMeshData = queuedEntityMeshData;
    }

    public GeneralEntityMeshData generate(EntityMeshWrapper entity, MeshShape[] meshShapes) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<UniqueVertex, Integer> vertexIndexMap = new HashMap<>();

        for (MeshShape meshShape : meshShapes) {
            meshShape.generate(vertices, indices, vertexIndexMap);
        }

        ByteBuffer vertexBuffer = MeshDataGenerator.createFloatBuffer(vertices);
        ByteBuffer indexBuffer = MeshDataGenerator.createIntBuffer(indices);

        return new GeneralEntityMeshData(vertexBuffer, indexBuffer, entity);
    }

    public EntityMeshWrapper generateMeshData(EntityMeshWrapper entityMeshWrapper) {
        EntityMeshDataDefinition definition = entityMeshDefinitionCache.get(entityMeshWrapper.entity().getEntityType(), null);

        EntityMeshData entityMeshData = null;

        if (definition == null) {
            Logger.debug("Creating mesh definition for entity: " + entityMeshWrapper.entity().getEntityType());
            queuedEntityMeshData.add(entityMeshWrapper.entity().getEntityType());

            if (entityMeshWrapper.entity().getEntityType() == EntityType.PLAYER) {
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

                GeneralEntityMeshData body = generate(entityMeshWrapper, new MeshShape[]{new BoxMeshShape(0, 0, 0, 1, 1.5f, 0.5f, bodyTexture)});

                GeneralEntityMeshData head = generate(entityMeshWrapper, new MeshShape[]{new BoxMeshShape(0, 0.5f, 0, 1, 1, 1, headTexture)});

                GeneralEntityMeshData leftArm = generate(entityMeshWrapper, new MeshShape[]{new BoxMeshShape(-0.25f, -0.75f, 0, 0.5f, 1.5f, 0.5f, leftArmTexture)});
                GeneralEntityMeshData rightArm = generate(entityMeshWrapper, new MeshShape[]{new BoxMeshShape(0.25f, -0.75f, 0, 0.5f, 1.5f, 0.5f, rightArmTexture)});

                GeneralEntityMeshData leftLeg = generate(entityMeshWrapper, new MeshShape[]{new BoxMeshShape(0, -0.75f, 0, 0.5f, 1.5f, 0.5f, leftLegTexture)});
                GeneralEntityMeshData rightLeg = generate(entityMeshWrapper, new MeshShape[]{new BoxMeshShape(0, -0.75f, 0, 0.5f, 1.5f, 0.5f, rightLegTexture)});

                body.addChild(head);
                body.addChild(leftArm);
                body.addChild(rightArm);
                body.addChild(leftLeg);
                body.addChild(rightLeg);

                entityMeshData = body;
            } else if (entityMeshWrapper.entity().getEntityType() == EntityType.PIG) {
                BoxTextureShape texture = new BoxTextureShape(256, 32);

                BoxTextureShape bodyTexture = texture.copy()
                        .setCoords(BlockFace.TOP, 0, 0, 0, 0)
                        .setCoords(BlockFace.BOTTOM, 0, 0, 0, 0)
                        .setCoords(BlockFace.NORTH, 0, 0, 0, 0)
                        .setCoords(BlockFace.SOUTH, 0, 0, 0, 0)
                        .setCoords(BlockFace.EAST, 0, 0, 0, 0)
                        .setCoords(BlockFace.WEST, 0, 0, 0, 0);

                entityMeshData = generate(entityMeshWrapper, new MeshShape[]{new BoxMeshShape(0, 0, 0, 1, 1, 1, bodyTexture)});
            }

            entityMeshDefinitionCache.put(entityMeshWrapper.entity().getEntityType(), new EntityMeshDataNoDefinition(entityMeshData));
        } else {
            entityMeshData = new ModelEntityMeshData(entityMeshWrapper);
            entityMeshWrapper.entity().setMesh(new EntityMesh(definition, entityMeshData));
        }

        if (entityMeshData != null) {
            entityMeshWrapper.setMeshData(entityMeshData);
        } else {
            Logger.warn("Entity mesh data is null.");
        }

        return entityMeshWrapper;
    }
}