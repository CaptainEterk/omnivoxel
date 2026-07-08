package omnivoxel.client.game.graphics.api.opengl.mesh.tasks;

import omnivoxel.client.game.graphics.api.opengl.mesh.MeshDataTask;
import omnivoxel.server.entity.ServerEntityShape;

public record EntityMeshDataTask(ServerEntityShape serverEntityShape) implements MeshDataTask {
    @Override
    public void reject() {

    }
}