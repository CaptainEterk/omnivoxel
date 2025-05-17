package omnivoxel.client.network.request;

import omnivoxel.math.Position3D;

public record ChunkRequest(Position3D position3D) implements Request {
    @Override
    public RequestType getType() {
        return RequestType.CHUNK;
    }
}