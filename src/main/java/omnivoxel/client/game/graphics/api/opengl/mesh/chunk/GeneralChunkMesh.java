package omnivoxel.client.game.graphics.api.opengl.mesh.chunk;

import omnivoxel.client.game.graphics.api.opengl.mesh.RenderMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.MeshData;

public record GeneralChunkMesh(
        RenderMesh solid,
        RenderMesh transparent,
        RenderMesh decoration,
        MeshData meshData
) implements ChunkMesh {
    @Override
    public void cleanup() {
        solid.cleanup();
        transparent.cleanup();
        decoration.cleanup();
    }
}