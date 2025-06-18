package omnivoxel.client.game.graphics.opengl.mesh;

public record EntityMesh(
        int solidVAO, int solidVBO, int solidEBO, int solidIndexCount
) implements Mesh {
}