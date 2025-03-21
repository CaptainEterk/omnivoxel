package omnivoxel.server;

public record Position3D(long x, long y, long z) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Position3D that = (Position3D) o;
        return x() == that.x() && y() == that.y() && z() == that.z();
    }

    public Position3D add(long x, long y, long z) {
        return new Position3D(this.x + x, this.y + y, this.z + z);
    }
}