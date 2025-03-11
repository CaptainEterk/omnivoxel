package omnivoxel.client.network.request;

import omnivoxel.client.game.position.ChunkPosition;

public record ChunkRequest(ChunkPosition chunkPosition) implements Request {
    @Override
    public RequestType getType() {
        return RequestType.CHUNK;
    }
}