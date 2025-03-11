package omnivoxel.client.game.mesh.chunk;

public record TransparentChunkMesh(
        int transparentVAO, int transparentVBO, int transparentEBO, int transparentIndexCount
) implements ChunkMesh {
    @Override
    public int solidVAO() {
        return 0;
    }

    @Override
    public int solidVBO() {
        return 0;
    }

    @Override
    public int solidEBO() {
        return 0;
    }

    @Override
    public int solidIndexCount() {
        return 0;
    }
}