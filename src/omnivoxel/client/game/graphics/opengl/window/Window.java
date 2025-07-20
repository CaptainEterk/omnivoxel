package omnivoxel.client.game.graphics.opengl.window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;

public final class Window {
    private final long window;
    private final String version;
    private final List<Consumer<Window>> matrixUsers;
    private int width;
    private int height;
    private float aspectRatio;

    public Window(long window, String version) {
        this.window = window;
        this.version = version;
        this.matrixUsers = new ArrayList<>();
    }

    public void init(int width, int height) {
        glfwSetWindowSizeCallback(window, new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                updateSize(width, height);
            }
        });
        updateSize(width, height);
    }

    private void updateSize(int width, int height) {
        setWidth(width);
        setHeight(height);
        setAspectRatio((float) width / height);

        glViewport(0, 0, width, height);

        updateMatrices();
    }

    public void fullscreen() {
        GLFW.glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
    }

    public void addMatrixListener(Consumer<Window> matrixUser) {
        matrixUsers.add(matrixUser);
    }

    public void updateMatrices() {
        for (Consumer<Window> matrixUser : matrixUsers) {
            matrixUser.accept(this);
        }
    }

    private void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public float aspectRatio() {
        return aspectRatio;
    }

    public void show() {
        glfwShowWindow(window);
        updateMatrices();
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    public long window() {
        return window;
    }

    public int getWidth() {
        return width;
    }

    private void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    private void setHeight(int height) {
        this.height = height;
    }

    public String getVersion() {
        return version;
    }
}