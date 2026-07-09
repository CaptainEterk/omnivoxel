package omnivoxel.server.entity;

import omnivoxel.server.client.ServerItem;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record EntityDefinition(String id, ServerEntityMesh entityMesh) implements ServerItem {
    @Override
    public byte[] getBytes() {
        byte[] idBytes = id.getBytes(StandardCharsets.UTF_8);
        byte[] meshBytes = entityMesh.id().getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(
                Integer.BYTES + idBytes.length +
                        Integer.BYTES + meshBytes.length
        );

        buffer.putInt(idBytes.length);
        buffer.put(idBytes);

        buffer.putInt(meshBytes.length);
        buffer.put(meshBytes);

        return buffer.array();
    }
}