package omnivoxel.client.game.graphics.api.opengl.mesh.chunk;

import omnivoxel.client.game.graphics.api.opengl.mesh.RenderMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.EmptyMeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.MeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.util.ChunkIndirectMemoryManager;
import omnivoxel.client.game.graphics.api.opengl.mesh.util.ChunkMeshBuffer;

public class EmptyChunkMesh implements ChunkMesh {
    private static final MeshData meshData = new EmptyMeshData();

    @Override
    public RenderMesh solid() {
        return RenderMesh.EMPTY;
    }

    @Override
    public RenderMesh transparent() {
        return RenderMesh.EMPTY;
    }

    @Override
    public RenderMesh decoration() {
        return RenderMesh.EMPTY;
    }

    @Override
    public int lod() {
        return 0;
    }

    @Override
    public MeshData meshData() {
        return meshData;
    }

    @Override
    public void cleanup(ChunkMeshBuffer chunkMeshBuffer) {
    }
}
