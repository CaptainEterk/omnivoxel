package omnivoxel.client.game.graphics.opengl.mesh.meshData;

import omnivoxel.client.game.entity.ClientEntity;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;

public record EntityMeshData(
        ByteBuffer solidVertices,
        ByteBuffer solidIndices,
        ClientEntity entity,
        List<EntityMeshData> children
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

    public void addChild(EntityMeshData entityMeshData) {
        children.add(entityMeshData);
    }
}