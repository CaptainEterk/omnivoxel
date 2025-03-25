package omnivoxel.server.world.chunk.result;

import omnivoxel.server.world.chunk.Chunk;

public record ChunkResult(byte[] bytes, Chunk chunk) {
}