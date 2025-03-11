package omnivoxel.client.game.mesh.chunk;

public class EmptyChunkMesh implements ChunkMesh {
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

    @Override
    public int transparentVAO() {
        return 0;
    }

    @Override
    public int transparentVBO() {
        return 0;
    }

    @Override
    public int transparentEBO() {
        return 0;
    }

    @Override
    public int transparentIndexCount() {
        return 0;
    }
}
