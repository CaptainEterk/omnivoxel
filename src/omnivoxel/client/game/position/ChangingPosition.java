package omnivoxel.client.game.position;

public final class ChangingPosition implements Position {
    private float x;
    private float y;
    private float z;

    public ChangingPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "ChangingPosition{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public void changeX(float x) {
        this.x += x;
    }

    public void changeY(float y) {
        this.y += y;
    }

    public void changeZ(float z) {
        this.z += z;
    }
}