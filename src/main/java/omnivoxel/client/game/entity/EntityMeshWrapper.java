package omnivoxel.client.game.entity;

import omnivoxel.client.game.graphics.api.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.server.entity.Entity;

public final class EntityMeshWrapper {
    private final Entity entity;
    private EntityMeshData meshData;

    public EntityMeshWrapper(Entity entity) {
        this.entity = entity;
        this.meshData = null;
    }

    public Entity entity() {
        return entity;
    }

    public EntityMeshData getMeshData() {
        return meshData;
    }

    public void setMeshData(EntityMeshData entityMeshData) {
        this.meshData = entityMeshData;
    }
}