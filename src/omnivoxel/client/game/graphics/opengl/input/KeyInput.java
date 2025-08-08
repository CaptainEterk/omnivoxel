package omnivoxel.client.game.graphics.opengl.input;

import org.lwjgl.glfw.GLFW;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyInput {
    private final Map<Integer, Boolean> keys;

    public KeyInput() {
        keys = new ConcurrentHashMap<>();
    }

    public void init(long window) {
        GLFW.glfwSetKeyCallback(window, this::keyCallback);
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            keys.put(key, true);
        } else if (action == GLFW_RELEASE) {
            keys.put(key, false);
        }
    }

    public boolean isKeyPressed(int key) {
        return keys.getOrDefault(key, false);
    }
}