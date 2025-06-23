package omnivoxel.server.client.structure;

import omnivoxel.math.Position3D;
import omnivoxel.util.boundingBox.WorldBoundingBox;

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

    public WorldBoundingBox toWorldBoundingBox(Position3D worldOrigin) {
        return new WorldBoundingBox(
                worldOrigin.x(),
                worldOrigin.y(),
                worldOrigin.z(),
                worldOrigin.x() + width - 1,
                worldOrigin.y() + height - 1,
                worldOrigin.z() + length - 1
        );
    }
}