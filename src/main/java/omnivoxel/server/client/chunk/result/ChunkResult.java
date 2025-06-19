package omnivoxel.server.client.chunk.result;

import omnivoxel.world.chunk.Chunk;

public record ChunkResult(byte[] bytes, Chunk chunk) {
}