package omnivoxel.world.block;

public record Block(String id, int[] blockState) {
    public Block(String id) {
        this(id, null);
    }
}