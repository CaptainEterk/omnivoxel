package omnivoxel.client.game.position;

import omnivoxel.client.game.settings.ConstantGameSettings;

public record ChunkPosition(int x, int y, int z) implements Position {
    public WorldPosition toWorldPosition(int x, int y, int z) {
        return new WorldPosition(
                this.x * ConstantGameSettings.CHUNK_WIDTH + x,
                this.y * ConstantGameSettings.CHUNK_HEIGHT + y,
                this.z * ConstantGameSettings.CHUNK_LENGTH + z
        );
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public String toString() {
        return "ChunkPosition{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public ChunkPosition add(ChangingChunkPosition changingChunkPosition) {
        return new ChunkPosition(
                x + changingChunkPosition.x(),
                y + changingChunkPosition.y(),
                z + changingChunkPosition.z()
        );
    }
}