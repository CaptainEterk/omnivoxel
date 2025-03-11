package omnivoxel.client.game.mesh.chunk;

import omnivoxel.client.game.mesh.Mesh;

public interface ChunkMesh extends Mesh {
    int solidVAO();

    int solidVBO();

    int solidEBO();

    int solidIndexCount();

    int transparentVAO();

    int transparentVBO();

    int transparentEBO();

    int transparentIndexCount();
}