package omnivoxel.client.game.position;

public record WorldPosition(int x, int y, int z) implements Position {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldPosition that = (WorldPosition) o;
        return x == that.x && y == that.y && z == that.z;
    }
}