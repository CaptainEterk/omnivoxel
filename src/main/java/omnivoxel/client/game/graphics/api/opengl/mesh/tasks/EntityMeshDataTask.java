package omnivoxel.client.game.graphics.api.opengl.mesh.tasks;

import omnivoxel.client.game.graphics.api.opengl.mesh.MeshDataTask;
import omnivoxel.common.resource.GameResources;
import omnivoxel.server.entity.ServerEntityMesh;

public record EntityMeshDataTask(ServerEntityMesh serverEntityMesh, GameResources gameResources) implements MeshDataTask {
    @Override
    public void reject() {
    }
}