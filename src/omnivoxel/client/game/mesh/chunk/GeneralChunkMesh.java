package omnivoxel.client.game.mesh.chunk;

import omnivoxel.client.game.thread.mesh.meshData.MeshData;

public record GeneralChunkMesh(
        int solidVAO, int solidVBO, int solidEBO, int solidIndexCount,
        int transparentVAO, int transparentVBO, int transparentEBO, int transparentIndexCount,
        MeshData meshData
) implements ChunkMesh {
}