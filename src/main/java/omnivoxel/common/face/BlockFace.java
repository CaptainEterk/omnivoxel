package omnivoxel.common.face;

public enum BlockFace {
    TOP,
    BOTTOM,
    NORTH,
    SOUTH,
    EAST,
    WEST,
    NONE;

    public static final BlockFace[] NORMAL_VALUES = new BlockFace[]{TOP, BOTTOM, NORTH, SOUTH, EAST, WEST};
}