package omnivoxel.server.io.entity;

import omnivoxel.server.io.CacheItem;
import omnivoxel.util.math.Position3D;

import java.util.Set;

public record EntityChunkCacheItem(Position3D position3D, Set<Long> entities) implements CacheItem {
}