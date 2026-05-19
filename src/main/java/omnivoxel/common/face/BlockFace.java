package omnivoxel.common.face;

import omnivoxel.common.settings.ConstantCommonSettings;

public enum BlockFace {
    TOP(1),
    BOTTOM(-1),
    NORTH(ConstantCommonSettings.PADDED_HEIGHT),
    SOUTH(-ConstantCommonSettings.PADDED_HEIGHT),
    EAST(ConstantCommonSettings.PADDED_LENGTH * ConstantCommonSettings.PADDED_HEIGHT),
    WEST(-ConstantCommonSettings.PADDED_LENGTH * ConstantCommonSettings.PADDED_HEIGHT),
    NONE(0);

    private final int paddedNeighborOffset;

    BlockFace(int paddedNeighborOffset) {
        this.paddedNeighborOffset = paddedNeighborOffset;
    }

    public int getPaddedNeighborOffset() {
        return paddedNeighborOffset;
    }
}