package omnivoxel.server.entity.mob;

import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.server.client.ServerItem;
import omnivoxel.server.entity.Entity;
import omnivoxel.server.entity.EntityType;
import omnivoxel.util.bytes.ByteUtils;

public class PlayerEntity extends MobEntity implements ServerItem {
    public PlayerEntity(String id) {
        super(id, id, new Hitbox(0, 0, 0, 1, 2, 1));
    }

    public static Entity decode(byte[] bytes) {
        int index = 0;
        int idLength = ByteUtils.getInt(bytes, index);
        index += Integer.BYTES;
        byte[] idBytes = new byte[idLength];
        System.arraycopy(bytes, index, idBytes, 0, idLength);
        index += idLength;
        String id = new String(idBytes);
        PlayerEntity entity = new PlayerEntity(id);
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
        byte[] idBytes = entityID.getBytes();

        int totalSize = Integer.BYTES + Integer.BYTES + idBytes.length + Double.BYTES * 5;

        byte[] out = new byte[totalSize];
        int offset = 0;

        ByteUtils.addInt(out, EntityType.PLAYER.ordinal(), offset);
        offset += Integer.BYTES;

        ByteUtils.addInt(out, idBytes.length, offset);
        offset += Integer.BYTES;

        System.arraycopy(idBytes, 0, out, offset, idBytes.length);
        offset += idBytes.length;

        ByteUtils.addDouble(out, x, offset);
        offset += Double.BYTES;
        ByteUtils.addDouble(out, y, offset);
        offset += Double.BYTES;
        ByteUtils.addDouble(out, z, offset);
        offset += Double.BYTES;
        ByteUtils.addDouble(out, pitch, offset);
        offset += Double.BYTES;
        ByteUtils.addDouble(out, yaw, offset);

        return out;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PLAYER;
    }
}