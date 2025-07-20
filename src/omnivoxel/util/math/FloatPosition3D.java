package omnivoxel.util.math;

public record FloatPosition3D(float x, float y, float z) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FloatPosition3D that = (FloatPosition3D) o;
        return x == that.x && y == that.y && z == that.z;
    }

    public FloatPosition3D add(float x, float y, float z) {
        return new FloatPosition3D(this.x + x, this.y + y, this.z + z);
    }

    public FloatPosition3D add(FloatPosition3D position3D) {
        return add(position3D.x(), position3D.y(), position3D.z());
    }
}