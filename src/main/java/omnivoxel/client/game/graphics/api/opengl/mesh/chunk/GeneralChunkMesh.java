package omnivoxel.client.game.graphics.api.opengl.mesh.chunk;

import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.MeshData;

public record GeneralChunkMesh(
        int solidVAO, int solidVBO, int solidEBO, int solidIndexCount,
        int transparentVAO, int transparentVBO, int transparentEBO, int transparentIndexCount,
        int decorationVAO, int decorationVBO, int decorationEBO, int decorationIndexCount,
        MeshData meshData
) implements ChunkMesh {
}