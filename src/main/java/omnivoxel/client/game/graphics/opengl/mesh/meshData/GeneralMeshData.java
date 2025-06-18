package omnivoxel.client.game.graphics.opengl.mesh.meshData;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public record GeneralMeshData(
        ByteBuffer solidVertices,
        ByteBuffer solidIndices,
        ByteBuffer transparentVertices,
        ByteBuffer transparentIndices
) implements MeshData {
    @Override
    public void cleanup() {
        // Free buffer data
        MemoryUtil.memFree(solidVertices);
        MemoryUtil.memFree(solidIndices);
        MemoryUtil.memFree(transparentVertices);
        MemoryUtil.memFree(transparentIndices);
    }
}