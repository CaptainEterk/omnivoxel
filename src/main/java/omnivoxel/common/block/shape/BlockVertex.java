package omnivoxel.common.block.shape;

public record BlockVertex(float px, float py, float pz) {
    public BlockVertex add(float x, float y, float z) {
        return new BlockVertex(x + px, y + py, z + pz);
    }
}