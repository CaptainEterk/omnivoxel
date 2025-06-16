package omnivoxel.client.game.thread.mesh.block.face;

import omnivoxel.client.game.settings.ConstantGameSettings;

public enum BlockFace {
    TOP(1),                                        // +Y
    BOTTOM(-1),                                    // -Y
    NORTH(ConstantGameSettings.PADDED_HEIGHT), // -Z
    SOUTH(-ConstantGameSettings.PADDED_HEIGHT),    // +Z
    EAST(ConstantGameSettings.PADDED_LENGTH * ConstantGameSettings.PADDED_HEIGHT), // +X
    WEST(-ConstantGameSettings.PADDED_LENGTH * ConstantGameSettings.PADDED_HEIGHT), // -X
    NONE(0);

    private final int paddedNeighborOffset;

    BlockFace(int paddedNeighborOffset) {
        this.paddedNeighborOffset = paddedNeighborOffset;
    }

    public int getPaddedNeighborOffset() {
        return paddedNeighborOffset;
    }


    public BlockFace opposite() {
        return switch (this) {
            case TOP -> BOTTOM;
            case BOTTOM -> TOP;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            case NONE -> NONE;
        };
    }

    public int getAxisX() {
        return switch (this) {
            case EAST -> 1;  // Positive X direction
            case WEST -> -1; // Negative X direction
            default -> 0;    // Other faces don't affect X
        };
    }

    public int getAxisY() {
        return switch (this) {
            case TOP -> 1;    // Positive Y direction
            case BOTTOM -> -1;// Negative Y direction
            default -> 0;     // Other faces don't affect Y
        };
    }

    public int getAxisZ() {
        return switch (this) {
            case NORTH -> -1; // Negative Z direction
            case SOUTH -> 1;  // Positive Z direction
            default -> 0;     // Other faces don't affect Z
        };
    }
}