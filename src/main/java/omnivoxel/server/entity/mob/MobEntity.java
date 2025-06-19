package omnivoxel.server.entity.mob;

import omnivoxel.client.game.hitbox.Hitbox;

public abstract class MobEntity extends GravityEntity {
    private final String name;
    protected float yaw;
    protected float pitch;

    protected MobEntity(String name, Hitbox hitbox) {
        super(hitbox);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void set(float x, float y, float z, float pitch, float yaw) {
        super.set(x, y, z);
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }
}