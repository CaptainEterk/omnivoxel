package omnivoxel.server.client.entity;

import omnivoxel.client.game.graphics.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.MeshData;
import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.math.FloatPosition3D;

public abstract class Entity {
    protected final float friction = getFriction();
    private final Hitbox hitbox;
    protected float x;
    protected float y;
    protected float z;
    protected float velocityX;
    protected float velocityY;
    protected float velocityZ;
    private MeshData meshData;
    protected EntityMesh mesh;

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

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    public MeshData getMeshData() {
        return meshData;
    }

    public Hitbox getHitbox() {
        return hitbox;
    }

    public EntityMesh getMesh() {
        return mesh;
    }

    public void setMesh(EntityMesh mesh) {
        this.mesh = mesh;
    }

    public void setMeshData(MeshData meshData) {
        this.meshData = meshData;
    }

    public FloatPosition3D getPosition() {
        return new FloatPosition3D(x, y, z);
    }
}