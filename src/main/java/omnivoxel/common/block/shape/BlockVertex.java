package omnivoxel.common.block.shape;

public final class BlockVertex {
    private float px;
    private float py;
    private float pz;

    public BlockVertex(float px, float py, float pz) {
        this.px = px;
        this.py = py;
        this.pz = pz;
    }

    public BlockVertex add(float x, float y, float z) {
        return new BlockVertex(x + px, y + py, z + pz);
    }

    public float px() {
        return px;
    }

    public float py() {
        return py;
    }

    public float pz() {
        return pz;
    }

    public void rotate(byte rotation) {
        float temp = px;
        switch (rotation & 3) {
            case 1:
                px = pz;
                pz = 1 - temp;
                break;
            case 2:
                px = 1 - px;
                pz = 1 - pz;
                break;
            case 3:
                px = 1 - pz;
                pz = temp;
        }
    }
}