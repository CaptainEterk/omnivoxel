package omnivoxel.server;

public record BlockIDCount(int blockID, int count) {
    @Override
    public String toString() {
        return "BlockIDCount{" +
                "blockID=" + blockID +
                ", count=" + count +
                '}';
    }
}