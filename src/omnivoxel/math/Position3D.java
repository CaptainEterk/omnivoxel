package omnivoxel.math;

public record Position3D(int x, int y, int z) {
    public static Position3D createFrom(Position3D position3D) {
        return new Position3D(position3D.x(), position3D.y(), position3D.z());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Position3D that = (Position3D) o;
        return x() == that.x() && y() == that.y() && z() == that.z();
    }

    public Position3D add(int x, int y, int z) {
        return new Position3D(this.x + x, this.y + y, this.z + z);
    }

    public Position3D add(Position3D position3D) {
        return add(position3D.x(), position3D.y(), position3D.z());
    }
}