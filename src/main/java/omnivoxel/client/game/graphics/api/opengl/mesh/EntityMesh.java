package omnivoxel.client.game.graphics.api.opengl.mesh;

import omnivoxel.client.game.graphics.api.opengl.mesh.definition.EntityMeshDefinition;
import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.EntityMeshData;

import java.util.ArrayList;
import java.util.List;

// TODO: There is a lot of shared data between this class and ServerEntityMesh, maybe combine?
// TODO: No transforms?
public final class EntityMesh implements Mesh {
    private final EntityMeshDefinition definition;
    private final List<EntityMesh> children;
    private final EntityMeshData entityMeshData;

    public EntityMesh(
            EntityMeshDefinition definition, EntityMeshData entityMeshData
    ) {
        this.definition = definition;
        this.entityMeshData = entityMeshData;
        children = new ArrayList<>();
    }

    public void addChild(EntityMesh child) {
        children.add(child);
    }

    public List<EntityMesh> getChildren() {
        return children;
    }

    public EntityMeshDefinition getDefinition() {
        return definition;
    }

    public EntityMeshData getMeshData() {
        return entityMeshData;
    }

    @Override
    public String toString() {
        return "EntityMesh{" +
                "definition=" + definition +
                ", children=" + children +
                ", entityMeshData=" + entityMeshData +
                '}';
    }
}