package omnivoxel.server.entity;

import omnivoxel.common.entity.EntityVertex;
import omnivoxel.server.client.ServerItem;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public record ServerEntityShape(String id, EntityVertex[][] entityVertices, int[][] indices) implements ServerItem {
    @Override
    public byte[] getBytes() {
        byte[] idBytes = id == null ? new byte[0] : id.getBytes(StandardCharsets.UTF_8);
        int idLen = idBytes.length;

        int capacity = 2 + idLen;
        for (int polygon = 0; polygon < 6; polygon++) {
            capacity += Short.BYTES;
            capacity += entityVertices[polygon].length * (5 * Float.BYTES);
            capacity += Short.BYTES;
            capacity += indices[polygon].length * Integer.BYTES;
        }

        ByteBuffer buffer = ByteBuffer.allocate(capacity).order(ByteOrder.BIG_ENDIAN);

        buffer.putShort((short) idLen);
        buffer.put(idBytes);

        for (int polygon = 0; polygon < 6; polygon++) {
            buffer.putShort((short) entityVertices[polygon].length);
            for (EntityVertex v : entityVertices[polygon]) {
                buffer.putFloat(v.x());
                buffer.putFloat(v.y());
                buffer.putFloat(v.z());
                buffer.putFloat(v.u());
                buffer.putFloat(v.v());
            }
            buffer.putShort((short) indices[polygon].length);
            for (int idx : indices[polygon]) {
                buffer.putInt(idx);
            }
        }

        return buffer.array();
    }
}