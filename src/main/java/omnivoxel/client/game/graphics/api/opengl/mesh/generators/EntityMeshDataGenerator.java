package omnivoxel.client.game.graphics.api.opengl.mesh.generators;

import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.GeneralEntityMeshData;
import omnivoxel.common.entity.EntityVertex;
import omnivoxel.common.resource.GameResources;
import omnivoxel.server.entity.ServerEntityMesh;
import omnivoxel.server.entity.ServerEntityShape;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityMeshDataGenerator {
    private final Map<String, EntityMeshData> entityMeshDataCache;

    public EntityMeshDataGenerator(Map<String, EntityMeshData> entityMeshDataCache) {
        this.entityMeshDataCache = entityMeshDataCache;
    }

    public EntityMeshData generateMeshData(ServerEntityMesh serverEntityMesh, GameResources gameResources) {
        if (entityMeshDataCache.containsKey(serverEntityMesh.shapeID())) {
            return entityMeshDataCache.get(serverEntityMesh.shapeID());
        }

        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<EntityVertex, Integer> vertexIndexMap = new HashMap<>();

        ServerEntityShape serverEntityShape = gameResources.serverEntityShapes().get(serverEntityMesh.shapeID());

        for (EntityVertex[] entityVertices : serverEntityShape.entityVertices()) {
            for (EntityVertex entityVertex : entityVertices) {
                int index;
                if (vertexIndexMap.containsKey(entityVertex)) {
                    index = vertexIndexMap.get(entityVertex);
                } else {
                    index = vertexIndexMap.size();
                    vertexIndexMap.put(entityVertex, index);
                    vertices.add(entityVertex.x());
                    vertices.add(entityVertex.y());
                    vertices.add(entityVertex.z());
                    vertices.add(entityVertex.u());
                    vertices.add(entityVertex.v());
                }
                indices.add(index);
            }
        }


        ByteBuffer vertexBuffer = MeshDataGenerator.createFloatBuffer(vertices);
        ByteBuffer indexBuffer = MeshDataGenerator.createIntBuffer(indices);

        EntityMeshData[] children = new EntityMeshData[serverEntityMesh.childrenIDs().length];
        for (int i = 0; i < children.length; i++) {
            children[i] = generateMeshData(gameResources.serverEntityMeshes().get(serverEntityMesh.childrenIDs()[i]), gameResources);
        }

        EntityMeshData meshData = new GeneralEntityMeshData(vertexBuffer, indexBuffer, children, serverEntityShape.id());

        entityMeshDataCache.put(serverEntityMesh.id(), meshData);

        return meshData;
    }
}