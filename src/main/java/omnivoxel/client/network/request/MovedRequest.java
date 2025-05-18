package omnivoxel.client.network.request;

public record MovedRequest(float x, float y, float z, float pitch, float yaw) implements Request {
    @Override
    public RequestType getType() {
        return RequestType.MOVE;
    }
}