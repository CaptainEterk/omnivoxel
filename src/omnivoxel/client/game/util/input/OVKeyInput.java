package omnivoxel.client.game.util.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class OVKeyInput {
    private final Map<Integer, Boolean> keys;
    private final List<PVKeyListener> keyListeners;

    public OVKeyInput() {
        keys = new HashMap<>();
        keyListeners = new ArrayList<>();
    }

    public void addKeyListener(PVKeyListener listener) {
        keyListeners.add(listener);
    }

    public OVKeyInput init(long window) {
        try (GLFWKeyCallback glfwKeyCallback = GLFW.glfwSetKeyCallback(window, this::keyCallback)) {

        }
        return this;
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            keys.put(key, true);
            keyListeners.forEach(listener -> listener.press(key, scancode, action, mods));
        } else if (action == GLFW_RELEASE) {
            keys.put(key, false);
            keyListeners.forEach(listener -> listener.release(key, scancode, action, mods));
        }
    }

    public boolean isKeyPressed(int key) {
        return keys.getOrDefault(key, false);
    }
}