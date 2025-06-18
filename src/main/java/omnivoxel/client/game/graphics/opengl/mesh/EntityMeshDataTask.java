package omnivoxel.client.game.graphics.opengl.mesh;

import omnivoxel.server.client.entity.Entity;

public record EntityMeshDataTask(Entity entity) implements MeshDataTask {
}