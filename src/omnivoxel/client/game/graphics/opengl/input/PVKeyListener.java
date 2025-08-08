package omnivoxel.client.game.graphics.opengl.input;

public interface PVKeyListener {
    void press(int key, int scancode, int action, int mods);

    void release(int key, int scancode, int action, int mods);
}