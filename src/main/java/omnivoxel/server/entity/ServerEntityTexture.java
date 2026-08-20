package omnivoxel.server.entity;

import omnivoxel.server.client.ServerItem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record ServerEntityTexture(String id) implements ServerItem {
    @Override
    public byte[] getBytes() {
        byte[] idBytes = id.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(
                Short.BYTES + idBytes.length
        ).order(ByteOrder.BIG_ENDIAN);

        buffer.putShort((short) idBytes.length);
        buffer.put(idBytes);

        return buffer.array();
    }
}