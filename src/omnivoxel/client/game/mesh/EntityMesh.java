package omnivoxel.client.game.mesh;

public record EntityMesh(
        int solidVAO, int solidVBO, int solidEBO, int solidIndexCount,
        int transparentVAO, int transparentVBO, int transparentEBO, int transparentIndexCount
) implements Mesh {
}