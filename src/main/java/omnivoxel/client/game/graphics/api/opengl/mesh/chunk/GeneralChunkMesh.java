package omnivoxel.client.game.graphics.api.opengl.mesh.chunk;

import omnivoxel.client.game.graphics.api.opengl.mesh.RenderMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.MeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.util.ChunkIndirectMemoryManager;
import omnivoxel.client.game.graphics.api.opengl.mesh.util.ChunkMeshBuffer;

public record GeneralChunkMesh(
        RenderMesh solid,
        RenderMesh transparent,
        RenderMesh decoration,
        int lod,
        MeshData meshData
) implements ChunkMesh {
    @Override
    public void cleanup(ChunkMeshBuffer chunkMeshBuffer) {
        chunkMeshBuffer.freeLater(solid.allocation());
        chunkMeshBuffer.freeLater(transparent.allocation());
        chunkMeshBuffer.freeLater(decoration.allocation());
    }
}