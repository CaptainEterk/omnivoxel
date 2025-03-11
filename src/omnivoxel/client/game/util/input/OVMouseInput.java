package omnivoxel.client.game.util.input;

import org.lwjgl.glfw.GLFWCursorPosCallback;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;

public class OVMouseInput {
    private double oldX;
    private double oldY;

    private double deltaX;
    private double deltaY;

    public OVMouseInput init(long window) {
        try (GLFWCursorPosCallback glfwCursorPosCallback = glfwSetCursorPosCallback(window, this::mouseCallback)) {

        }
        return this;
    }

    private void mouseCallback(long window, double x, double y) {
        deltaX = x - oldX;
        deltaY = y - oldY;
        oldX = x;
        oldY = y;
    }

    public double getDeltaX() {
        return deltaX;
    }

    public double getDeltaY() {
        return deltaY;
    }

    public void clearDelta() {
        deltaX = 0;
        deltaY = 0;
    }
}