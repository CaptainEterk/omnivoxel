package omnivoxel.server.client.structure;

import omnivoxel.math.Position3D;

public class StructureBoundingBox {
    protected final int width;
    protected final int height;
    protected final int length;

    private StructureBoundingBox(int width, int height, int length) {
        this.width = width;
        this.height = height;
        this.length = length;
    }

    public static StructureBoundingBox create(int width, int height, int length) {
        return new StructureBoundingBox(width, height, length);
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public final int getLength() {
        return length;
    }

    public boolean contains(int x, int y, int z, Position3D structureOffset) {
        // Calculate the relative coordinates within the structure's bounding box
        int relX = x - structureOffset.x();
        int relY = y - structureOffset.y();
        int relZ = z - structureOffset.z();

        // Check if the relative coordinates are within the bounds of the structure
        return relX >= 0 && relX < width &&
                relY >= 0 && relY < height &&
                relZ >= 0 && relZ < length;
    }
}