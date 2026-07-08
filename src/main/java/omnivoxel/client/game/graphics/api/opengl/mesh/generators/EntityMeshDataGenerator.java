package omnivoxel.client.game.graphics.api.opengl.mesh.generators;

import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.GeneralEntityMeshData;
import omnivoxel.common.entity.EntityVertex;
import omnivoxel.server.entity.ServerEntityShape;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityMeshDataGenerator {
    // TODO: Add EntityShape here
    public EntityMeshData generateMeshData(ServerEntityShape serverEntityShape) {
        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        Map<EntityVertex, Integer> vertexIndexMap = new HashMap<>();

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

        return new GeneralEntityMeshData(vertexBuffer, indexBuffer, serverEntityShape.id());
    }
}