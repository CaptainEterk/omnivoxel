package omnivoxel.client.game.graphics.opengl.mesh.meshData;

import omnivoxel.server.client.entity.Entity;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public record EntityMeshData(
        ByteBuffer solidVertices,
        ByteBuffer solidIndices,
        Entity entity
) implements MeshData {
    @Override
    public ByteBuffer transparentVertices() {
        return null;
    }

    @Override
    public ByteBuffer transparentIndices() {
        return null;
    }

    @Override
    public void cleanup() {
        // Free buffer data
        MemoryUtil.memFree(solidVertices);
        MemoryUtil.memFree(solidIndices);
    }
}