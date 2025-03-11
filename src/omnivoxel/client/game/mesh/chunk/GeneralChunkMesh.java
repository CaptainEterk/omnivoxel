package omnivoxel.client.game.mesh.chunk;

public record GeneralChunkMesh(
        int solidVAO, int solidVBO, int solidEBO, int solidIndexCount,
        int transparentVAO, int transparentVBO, int transparentEBO, int transparentIndexCount
) implements ChunkMesh {
}