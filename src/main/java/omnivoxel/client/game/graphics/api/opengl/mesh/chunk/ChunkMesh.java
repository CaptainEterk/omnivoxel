package omnivoxel.client.game.graphics.api.opengl.mesh.chunk;

import omnivoxel.client.game.graphics.api.opengl.mesh.Mesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.RenderMesh;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.MeshData;
import omnivoxel.client.game.graphics.api.opengl.mesh.util.ChunkIndirectMemoryManager;
import omnivoxel.client.game.graphics.api.opengl.mesh.util.ChunkMeshBuffer;

// TODO: Split into three meshes: solid, transparent, decoration
public interface ChunkMesh extends Mesh {
    RenderMesh solid();

    RenderMesh transparent();

    RenderMesh decoration();

    int lod();

    MeshData meshData();

    void cleanup(ChunkMeshBuffer memoryManager);
}