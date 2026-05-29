package omnivoxel.client.game.graphics.api.opengl.mesh.tasks;

import omnivoxel.client.game.entity.EntityMeshWrapper;
import omnivoxel.client.game.graphics.api.opengl.mesh.MeshDataTask;

public record EntityMeshDataTask(EntityMeshWrapper entity) implements MeshDataTask {
    @Override
    public void reject() {

    }
}