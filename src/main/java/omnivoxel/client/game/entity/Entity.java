package omnivoxel.client.game.entity;

import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;

public abstract class Entity {
    protected final float friction = getFriction();
    private final Hitbox hitbox;
    protected float x;
    protected float y;
    protected float z;
    protected float velocityX;
    protected float velocityY;
    protected float velocityZ;
    protected float yaw;
    protected float pitch;
    private MeshData meshData;

    protected Entity(Hitbox hitbox) {
        this.hitbox = hitbox;
    }

    protected float getFriction() {
        return 0.1f;
    }

    public void tick(float deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        z += velocityZ * deltaTime;

        float frictionFactor = (float) Math.pow(friction, deltaTime);
        velocityX *= frictionFactor;
        velocityY *= frictionFactor;
        velocityZ *= frictionFactor;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public float getVelocityZ() {
        return velocityZ;
    }

    public void setVelocityZ(float velocityZ) {
        this.velocityZ = velocityZ;
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

    public MeshData getMeshData() {
        return meshData;
    }

    public void setMeshData(MeshData meshData) {
        this.meshData = meshData;
    }
}