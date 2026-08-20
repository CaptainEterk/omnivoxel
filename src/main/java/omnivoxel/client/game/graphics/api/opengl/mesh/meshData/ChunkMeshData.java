package omnivoxel.client.game.graphics.api.opengl.mesh.meshData;

import omnivoxel.util.math.Position3D;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public record ChunkMeshData(
        ByteBuffer solidVertices,
        ByteBuffer solidIndices,
        ByteBuffer transparentVertices,
        ByteBuffer transparentIndices,
        ByteBuffer decorationVertices,
        ByteBuffer decorationIndices,
        Position3D chunkPosition
) implements MeshData {
    @Override
    public void cleanup() {
        if (solidVertices != null) MemoryUtil.memFree(solidVertices);
        if (solidIndices != null) MemoryUtil.memFree(solidIndices);
        if (transparentVertices != null) MemoryUtil.memFree(transparentVertices);
        if (transparentIndices != null) MemoryUtil.memFree(transparentIndices);
        if (decorationVertices != null) MemoryUtil.memFree(decorationVertices);
        if (decorationIndices != null) MemoryUtil.memFree(decorationIndices);
    }
}