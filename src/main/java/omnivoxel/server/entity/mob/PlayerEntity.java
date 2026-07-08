package omnivoxel.server.entity.mob;

import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.server.client.ServerItem;
import omnivoxel.server.entity.Entity;
import omnivoxel.server.entity.EntityType;
import omnivoxel.util.bytes.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PlayerEntity extends MobEntity implements ServerItem {
    public PlayerEntity(String id, String meshID) {
        super(id, id, meshID, new Hitbox(0, 0, 0, 1, 2, 1));
    }

    public static Entity decode(byte[] bytes) {
        int index = 0;

        int idLength = ByteUtils.getInt(bytes, index);
        index += Integer.BYTES;

        byte[] idBytes = new byte[idLength];
        System.arraycopy(bytes, index, idBytes, 0, idLength);
        index += idLength;

        String id = new String(idBytes, StandardCharsets.UTF_8);

        int meshIDLength = ByteUtils.getInt(bytes, index);
        index += Integer.BYTES;

        byte[] meshIDBytes = new byte[meshIDLength];
        System.arraycopy(bytes, index, meshIDBytes, 0, meshIDLength);
        index += meshIDLength;

        String meshID = new String(meshIDBytes, StandardCharsets.UTF_8);

        PlayerEntity entity = new PlayerEntity(id, meshID);

        entity.setX(ByteUtils.getDouble(bytes, index));
        index += Double.BYTES;

        entity.setY(ByteUtils.getDouble(bytes, index));
        index += Double.BYTES;

        entity.setZ(ByteUtils.getDouble(bytes, index));
        index += Double.BYTES;

        entity.setPitch(ByteUtils.getDouble(bytes, index));
        index += Double.BYTES;

        entity.setYaw(ByteUtils.getDouble(bytes, index));

        return entity;
    }

    @Override
    public byte[] getBytes() {
        byte[] idBytes = entityID.getBytes(StandardCharsets.UTF_8);
        byte[] meshIDBytes = meshID.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(
                Integer.BYTES +
                        Integer.BYTES + idBytes.length +
                        Integer.BYTES + meshIDBytes.length +
                        Double.BYTES * 5
        );

        buffer.putInt(EntityType.PLAYER.ordinal());

        buffer.putInt(idBytes.length);
        buffer.put(idBytes);

        buffer.putInt(meshIDBytes.length);
        buffer.put(meshIDBytes);

        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);
        buffer.putDouble(pitch);
        buffer.putDouble(yaw);

        return buffer.array();
    }
}