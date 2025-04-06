package omnivoxel.client.game.mesh.chunk;

import omnivoxel.client.game.thread.mesh.meshData.MeshData;

public record TransparentChunkMesh(
        int transparentVAO, int transparentVBO, int transparentEBO, int transparentIndexCount,
        MeshData meshData
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