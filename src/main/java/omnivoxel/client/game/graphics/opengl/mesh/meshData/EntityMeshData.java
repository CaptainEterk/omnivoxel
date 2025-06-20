package omnivoxel.client.game.graphics.opengl.mesh.meshData;

import omnivoxel.client.game.entity.ClientEntity;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;

public final class EntityMeshData implements MeshData {
    private final ByteBuffer solidVertices;
    private final ByteBuffer solidIndices;
    private final ClientEntity entity;
    private final List<EntityMeshData> children;
    private Matrix4f model = new Matrix4f();

    public EntityMeshData(
            ByteBuffer solidVertices,
            ByteBuffer solidIndices,
            ClientEntity entity,
            List<EntityMeshData> children
    ) {
        this.solidVertices = solidVertices;
        this.solidIndices = solidIndices;
        this.entity = entity;
        this.children = children;
    }

    public Matrix4f getModel() {
        return model;
    }

    public void setModel(Matrix4f model) {
        this.model = model;
    }

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

    @Override
    public ByteBuffer solidVertices() {
        return solidVertices;
    }

    @Override
    public ByteBuffer solidIndices() {
        return solidIndices;
    }

    public ClientEntity entity() {
        return entity;
    }

    public List<EntityMeshData> children() {
        return children;
    }
}