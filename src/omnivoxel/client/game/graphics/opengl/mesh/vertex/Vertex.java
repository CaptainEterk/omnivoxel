package omnivoxel.client.game.graphics.opengl.mesh.vertex;

import omnivoxel.client.game.graphics.opengl.shape.util.ShapeHelper;

public record Vertex(float px, float py, float pz) {
    public Vertex(float px, float py, float pz) {
        this.px = px;
        this.py = py;
        this.pz = pz;
        if (px / ShapeHelper.PIXEL % 1 != 0) {
            System.out.println(px);
        }
    }

    public Vertex add(float x, float y, float z) {
        return new Vertex(x + px, y + py, z + pz);
    }
}