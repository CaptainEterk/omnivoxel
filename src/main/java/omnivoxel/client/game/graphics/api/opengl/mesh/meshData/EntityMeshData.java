package omnivoxel.client.game.graphics.api.opengl.mesh.meshData;

import org.joml.Matrix4f;

import java.util.List;

public interface EntityMeshData extends MeshData {
    Matrix4f getModel();

    void setModel(Matrix4f model);

    EntityMeshData[] children();

    String id();
}