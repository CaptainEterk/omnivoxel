package omnivoxel.client.game.graphics.api.opengl.mesh.meshData;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public final class GeneralEntityMeshData implements EntityMeshData {
    private final ByteBuffer solidVertices;
    private final ByteBuffer solidIndices;
    private final EntityMeshData[] children;
    private final String id;
    private Matrix4f model = new Matrix4f();

    public GeneralEntityMeshData(
            ByteBuffer solidVertices,
            ByteBuffer solidIndices, EntityMeshData[] children,
            String id
    ) {
        this.solidVertices = solidVertices;
        this.solidIndices = solidIndices;
        this.children = children;
        this.id = id;
    }

    @Override
    public Matrix4f getModel() {
        return model;
    }

    @Override
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
        MemoryUtil.memFree(solidVertices);
        MemoryUtil.memFree(solidIndices);
    }

    @Override
    public ByteBuffer solidVertices() {
        return solidVertices;
    }

    @Override
    public ByteBuffer solidIndices() {
        return solidIndices;
    }

    @Override
    public EntityMeshData[] children() {
        return children;
    }

    @Override
    public String id() {
        return id;
    }
}