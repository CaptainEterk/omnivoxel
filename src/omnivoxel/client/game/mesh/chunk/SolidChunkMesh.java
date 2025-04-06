package omnivoxel.client.game.mesh.chunk;

import omnivoxel.client.game.thread.mesh.meshData.MeshData;

public record SolidChunkMesh(
        int solidVAO, int solidVBO, int solidEBO, int solidIndexCount,
        MeshData meshData
) implements ChunkMesh {
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
