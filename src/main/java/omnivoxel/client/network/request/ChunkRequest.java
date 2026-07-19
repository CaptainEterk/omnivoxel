package omnivoxel.client.network.request;

import omnivoxel.util.math.Position3D;

public record ChunkRequest(Position3D position3D, int lod) implements Request {
    @Override
    public RequestType getType() {
        return RequestType.CHUNK;
    }
}