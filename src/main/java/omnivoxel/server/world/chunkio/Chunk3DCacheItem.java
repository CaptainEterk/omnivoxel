package omnivoxel.server.world.chunkio;

import omnivoxel.server.client.block.ServerBlock;
import omnivoxel.util.math.Position3D;
import omnivoxel.world.chunk.Chunk;

public record Chunk3DCacheItem(Position3D chunkPosition, Chunk<ServerBlock> chunk) implements ChunkCacheItem {
}