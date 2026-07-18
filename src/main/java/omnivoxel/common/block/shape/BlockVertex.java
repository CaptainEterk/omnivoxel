package omnivoxel.common.block.shape;

public record BlockVertex(float px, float py, float pz) {
    public BlockVertex add(float x, float y, float z) {
        return new BlockVertex(x + px, y + py, z + pz);
    }

    public BlockVertex rotate(byte rotation) {
        return switch (rotation & 3) {
            case 1 -> new BlockVertex(pz, py, 1 - px);
            case 2 -> new BlockVertex(1 - px, py, 1 - pz);
            case 3 -> new BlockVertex(1 - pz, py, px);
            default -> this;
        };
    }
}