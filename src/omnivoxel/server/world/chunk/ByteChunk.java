package omnivoxel.server.world.chunk;

import omnivoxel.server.world.chunk.result.GeneratedChunk;

public record ByteChunk(byte[] bytes, GeneratedChunk chunk) {
}