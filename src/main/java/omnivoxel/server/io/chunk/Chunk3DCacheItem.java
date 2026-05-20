package omnivoxel.server.io.chunk;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.server.io.CacheItem;
import omnivoxel.util.math.Position3D;
import omnivoxel.world.chunk.Chunk;

public record Chunk3DCacheItem(Position3D chunkPosition, Chunk<ServerBlock> chunk) implements CacheItem {
}