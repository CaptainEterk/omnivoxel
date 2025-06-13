package omnivoxel.client.game.hitbox;

public class Hitbox {
    private final float minX, minY, minZ;
    private final float maxX, maxY, maxZ;

    public Hitbox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public boolean intersects(Hitbox other) {
        return this.maxX > other.minX && this.minX < other.maxX && this.maxY > other.minY && this.minY < other.maxY && this.maxZ > other.minZ && this.minZ < other.maxZ;
    }
}