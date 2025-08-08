package omnivoxel.client.game.graphics.opengl.mesh;

import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;

public interface MeshDataTask {
    default void finished(MeshData meshData) {
    }

    default void cleanup() {
    }
}