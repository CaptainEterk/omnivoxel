package omnivoxel.client.game.graphics.api.opengl.mesh.meshData;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class GeneralEntityMeshData implements EntityMeshData {
    private final ByteBuffer solidVertices;
    private final ByteBuffer solidIndices;
    private final List<EntityMeshData> children;
    private final String id;
    private Matrix4f model = new Matrix4f();

    public GeneralEntityMeshData(
            ByteBuffer solidVertices,
            ByteBuffer solidIndices,
            String id
    ) {
        this.solidVertices = solidVertices;
        this.solidIndices = solidIndices;
        this.id = id;
        this.children = new ArrayList<>();
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
    public List<EntityMeshData> children() {
        return children;
    }

    @Override
    public String id() {
        return id;
    }
}