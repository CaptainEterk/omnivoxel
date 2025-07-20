package omnivoxel.client.game.graphics.opengl.mesh;

import omnivoxel.client.game.entity.ClientEntity;

public record EntityMeshDataTask(ClientEntity entity) implements MeshDataTask {
}