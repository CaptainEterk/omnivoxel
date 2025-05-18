package omnivoxel.client.game.util.input;

public interface PVKeyListener {
    void press(int key, int scancode, int action, int mods);

    void release(int key, int scancode, int action, int mods);
}