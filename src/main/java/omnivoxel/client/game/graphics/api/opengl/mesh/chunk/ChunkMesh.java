package omnivoxel.client.game.graphics.api.opengl.mesh.chunk;

import omnivoxel.client.game.graphics.api.opengl.mesh.Mesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.RenderMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.MeshData;

// TODO: Split into three meshes: solid, transparent, decoration
public interface ChunkMesh extends Mesh {
    RenderMesh solid();

    RenderMesh transparent();

    RenderMesh decoration();

    MeshData meshData();

    void cleanup();
}