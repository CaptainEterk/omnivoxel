package omnivoxel.client.game.entity;

import omnivoxel.client.game.position.ChangingPosition;
import omnivoxel.client.game.thread.mesh.meshData.MeshData;

public abstract class Entity {
    protected final float friction;
    protected final ChangingPosition changingPosition;
    protected float velocityX;
    protected float velocityY;
    protected float velocityZ;
    protected float yaw;
    protected float pitch;
    private MeshData meshData;

    protected Entity(float friction) {
        this.friction = friction;
        this.changingPosition = new ChangingPosition(0, 0, 0);
    }

    public void tick(float deltaTime) {
        // Calculate sin and cos
        float sinYaw = org.joml.Math.sin(yaw);
        float cosYaw = org.joml.Math.cos(yaw);

        // Calculate movement from yaw
        float moveX = velocityZ * sinYaw +
                velocityX * cosYaw;
        float moveZ = velocityZ * cosYaw +
                velocityX * -sinYaw;

        // Apply movement to position
        // TODO: Implement gravity
        changingPosition.changeX(-moveX);
        changingPosition.changeY(-velocityY);
        changingPosition.changeZ(moveZ);

        velocityX *= friction;
        velocityY *= friction;
        velocityZ *= friction;
    }

    public float getX() {
        return changingPosition.x();
    }

    public void setX(float x) {
        changingPosition.setX(x);
    }

    public void changeX(float x) {
        changingPosition.changeX(x);
    }

    public float getY() {
        return changingPosition.y();
    }

    public void setY(float y) {
        changingPosition.setY(y);
    }

    public void changeY(float y) {
        changingPosition.changeY(y);
    }

    public float getZ() {
        return changingPosition.z();
    }

    public void setZ(float z) {
        changingPosition.setZ(z);
    }

    public void changeZ(float z) {
        changingPosition.changeZ(z);
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

    public ChangingPosition getPosition() {
        return changingPosition;
    }
}