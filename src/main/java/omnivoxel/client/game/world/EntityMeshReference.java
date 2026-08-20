package omnivoxel.client.game.world;

import omnivoxel.client.game.graphics.api.opengl.mesh.EntityMesh;

public class EntityMeshReference {
    private EntityMesh entityMesh;

    public EntityMeshReference(EntityMesh entityMesh) {
        this.entityMesh = entityMesh;
    }

    public EntityMeshReference() {
        this(null);
    }

    public EntityMesh getEntityMesh() {
        return entityMesh;
    }

    public void setEntityMesh(EntityMesh entityMesh) {
        this.entityMesh = entityMesh;
    }
}