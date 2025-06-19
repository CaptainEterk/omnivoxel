package omnivoxel.client.game.entity;

import omnivoxel.client.game.graphics.opengl.mesh.EntityMesh;
import omnivoxel.client.game.graphics.opengl.mesh.meshData.EntityMeshData;
import omnivoxel.server.entity.EntityType;

public class ClientEntity {
    private final String name;
    private final String uuid;
    private final EntityType type;
    private float x;
    private float y;
    private float z;
    private float yaw;
    private float pitch;
    private EntityMesh mesh;
    private EntityMeshData meshData;

    public ClientEntity(String name, String uuid, EntityType type) {
        this.name = name;
        this.uuid = uuid;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return uuid;
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

    public EntityMesh getMesh() {
        return mesh;
    }

    public void setMesh(EntityMesh mesh) {
        this.mesh = mesh;
    }

    public EntityMeshData getMeshData() {
        return meshData;
    }

    public void setMeshData(EntityMeshData meshData) {
        this.meshData = meshData;
    }

    public EntityType getType() {
        return type;
    }
}