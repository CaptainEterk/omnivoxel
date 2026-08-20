package omnivoxel.client.game.entity;

import omnivoxel.client.game.world.EntityMeshReference;
import omnivoxel.server.entity.Entity;

public record EntityMeshWrapper(Entity entity, EntityMeshReference entityMeshReference) {
}