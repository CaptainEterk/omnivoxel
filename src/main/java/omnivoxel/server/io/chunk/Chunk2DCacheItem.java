package omnivoxel.server.io.chunk;

import omnivoxel.util.math.Position2D;
import omnivoxel.world.chunk2d.Chunk2D;

public record Chunk2DCacheItem(Position2D chunkPosition, Chunk2D<Integer> chunk) implements ChunkCacheItem {
}