package omnivoxel.client.game.graphics.api.opengl.mesh;

import org.lwjgl.opengl.GL30C;

public record RenderMesh(int vao, int vbo, int ebo, int indexCount) {
    public static final RenderMesh EMPTY = new RenderMesh(0, 0, 0, 0);

    public void cleanup() {
        GL30C.glDeleteVertexArrays(vao);
        GL30C.glDeleteBuffers(vbo);
        GL30C.glDeleteBuffers(ebo);
    }
}