package omnivoxel.world.block;

public final class Block {
    private final String id;
    private final int[] blockState;

    public Block(String id, int[] blockState) {
        this.id = id;
        this.blockState = blockState;
    }

    public Block(String id) {
        this(id, null);
    }

    public String id() {
        return id;
    }

    public int[] blockState() {
        return blockState;
    }
}