package omnivoxel.server.client.chunk;

import omnivoxel.server.client.chunk.result.GeneralGeneratedChunk;

public record ByteChunk(byte[] bytes, GeneralGeneratedChunk chunk) {
}