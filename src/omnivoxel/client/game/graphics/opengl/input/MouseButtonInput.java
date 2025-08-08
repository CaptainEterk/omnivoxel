package omnivoxel.client.game.graphics.opengl.input;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.glfw.GLFW.*;

public class MouseButtonInput {
    private final Map<Integer, Boolean> mouseButtons;
    private final AtomicBoolean mouseLocked;

    public MouseButtonInput() {
        mouseButtons = new HashMap<>();
        mouseLocked = new AtomicBoolean();
    }

    public void init(long window) {
        glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
    }

    public void mouseButtonCallback(long window, int button, int action, int mods) {
        if (action == GLFW_PRESS) {
            mouseButtons.put(button, true);
        } else if (action == GLFW_RELEASE) {
            mouseButtons.put(button, false);
        }
    }

    public boolean isMouseButtonPressed(int mouseButton) {
        return mouseButtons.getOrDefault(mouseButton, false);
    }

    public void lockMouse(long window) {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        mouseLocked.set(true);
    }

    public void unlockMouse(long window) {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        mouseLocked.set(false);
    }

    public boolean isMouseLocked() {
        return mouseLocked.get();
    }
}