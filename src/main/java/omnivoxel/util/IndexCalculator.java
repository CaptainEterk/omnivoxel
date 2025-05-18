package omnivoxel.util;

import omnivoxel.client.game.settings.ConstantGameSettings;

public class IndexCalculator {
    public static int calculateBlockIndex(int x, int y, int z) {
        return (x + 1) * ConstantGameSettings.CHUNK_WIDTH * ConstantGameSettings.CHUNK_LENGTH + (z + 1) * ConstantGameSettings.CHUNK_LENGTH + (y + 1);
    }

    public static int calculateBlockIndexPadded(int x, int y, int z) {
        return (x + 1) * (ConstantGameSettings.CHUNK_WIDTH + 2) * (ConstantGameSettings.CHUNK_LENGTH + 2) + (z + 1) * (ConstantGameSettings.CHUNK_LENGTH + 2) + (y + 1);
    }
}