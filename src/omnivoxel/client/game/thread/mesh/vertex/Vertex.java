package omnivoxel.client.game.thread.mesh.vertex;

public record Vertex(float px, float py, float pz) {
    public Vertex add(float x, float y, float z) {
        return new Vertex(x + px, y + py, z + pz);
    }

    @Override
    public String toString() {
        return "Vertex{" +
                "px=" + px +
                ", py=" + py +
                ", pz=" + pz +
                '}';
    }
}