package omnivoxel.client.game.mesh.chunk;

import omnivoxel.client.game.mesh.Mesh;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;

public interface ChunkMesh extends Mesh {
    int solidVAO();

    int solidVBO();

    int solidEBO();

    int solidIndexCount();

    int transparentVAO();

    int transparentVBO();

    int transparentEBO();

    int transparentIndexCount();

    MeshData meshData();
}