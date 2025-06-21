package core.entities;

import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.server.entity.EntityType;
import omnivoxel.server.entity.mob.MobEntity;
import omnivoxel.util.bytes.ByteUtils;

public class PigEntity extends MobEntity {
    private final static Hitbox hitbox = new Hitbox(0, 0, 0, 1, 1, 2);

    public PigEntity(String name) {
        super(name, hitbox);
    }

    public PigEntity() {
        this("");
    }

    @Override
    public byte[] getBytes() {
        String name = getName();
        int length = name.length();
        byte[] out = new byte[entityID.length + length + 32];
        ByteUtils.addInt(out, entityID.length, 0);
        System.arraycopy(entityID, 0, out, 4, entityID.length);

        ByteUtils.addFloats(out, 36, x, y, z, pitch, yaw);

        ByteUtils.addInt(out, length, 56);
        System.arraycopy(name.getBytes(), 0, out, 60, length);
        ByteUtils.addInt(out, EntityType.Type.PLAYER.ordinal(), 60 + length);
        return out;
    }
}