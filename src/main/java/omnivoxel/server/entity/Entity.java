package omnivoxel.server.entity;

import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.server.client.ServerItem;
import omnivoxel.util.math.DoublePosition3D;

public abstract class Entity implements ServerItem {
    protected final float friction = getFriction();
    protected final String entityID;
    protected final String meshID;
    private final Hitbox hitbox;
    protected double x;
    protected double y;
    protected double z;
    protected double velocityX;
    protected double velocityY;
    protected double velocityZ;
    protected double pitch;
    protected double yaw;

    protected Entity(String entityID, String meshID, Hitbox hitbox) {
        this.entityID = entityID;
        this.meshID = meshID;
        this.hitbox = hitbox;
    }

    public String getMeshID() {
        return meshID;
    }

    protected float getFriction() {
        return 0.1f;
    }

    public void tick(float deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        z += velocityZ * deltaTime;

        double frictionFactor = Math.pow(friction, deltaTime);
        velocityX *= frictionFactor;
        velocityY *= frictionFactor;
        velocityZ *= frictionFactor;
    }

    public String getEntityID() {
        return entityID;
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(double x, double y, double z, double pitch, double yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public double getVelocityZ() {
        return velocityZ;
    }

    public void setVelocityZ(double velocityZ) {
        this.velocityZ = velocityZ;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public DoublePosition3D getPosition() {
        return new DoublePosition3D(x, y, z);
    }
}