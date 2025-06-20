package omnivoxel.client.game.graphics.opengl.mesh;

import omnivoxel.client.game.graphics.opengl.mesh.definition.EntityMeshDataDefinition;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class EntityMesh implements Mesh {
    private final EntityMeshDataDefinition definition;
    private Matrix4f model = new Matrix4f();
    private final List<EntityMesh> children;

    public EntityMesh(
            EntityMeshDataDefinition definition
    ) {
        this.definition = definition;
        children = new ArrayList<>();
    }

    public void addChild(EntityMesh child) {
        children.add(child);
    }

    public List<EntityMesh> getChildren() {
        return children;
    }

    public Matrix4f getModel() {
        return model;
    }

    public void setModel(Matrix4f model) {
        System.out.println(this + " " + model);
        this.model = model;
    }

    public EntityMeshDataDefinition getDefinition() {
        return definition;
    }
}