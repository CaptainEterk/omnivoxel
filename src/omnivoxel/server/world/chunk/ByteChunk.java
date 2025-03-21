package omnivoxel.server.world.chunk;

import omnivoxel.server.world.chunk.padded.GeneratedChunk;

public record ByteChunk(byte[] bytes, GeneratedChunk chunk) {
}