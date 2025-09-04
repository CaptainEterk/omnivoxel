package omnivoxel.world.block;

public record Block(String id, String blockState) {
    public Block(String id) {
        this(id, null);
    }
}