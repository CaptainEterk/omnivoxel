package omnivoxel.server.chunk;

import omnivoxel.server.chunk.result.GeneralGeneratedChunk;

public record ByteChunk(byte[] bytes, GeneralGeneratedChunk chunk) {
}