package omnivoxel.server.io.entity;

import omnivoxel.server.entity.Entity;
import omnivoxel.server.io.CacheItem;
import omnivoxel.util.math.Position3D;

public record EntityCacheItem(Position3D position3D, Entity entity) implements CacheItem {
}