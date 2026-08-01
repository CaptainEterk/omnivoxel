package omnivoxel.client.game.graphics.api.opengl.window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;

public final class Window {
    private final long window;
    private final String version;
    private final List<Consumer<Window>> resizingCallbacks;
    private final Queue<Consumer<Window>> contextTasks;
    private int width;
    private int height;
    private float aspectRatio;

    private int oldWindowWidth;
    private int oldWindowHeight;
    private int oldWindowX;
    private int oldWindowY;

    public Window(long window, String version, Queue<Consumer<Window>> contextTasks) {
        this.window = window;
        this.version = version;
        this.contextTasks = contextTasks;
        this.resizingCallbacks = new ArrayList<>();
    }

    public void init(int width, int height) {
        glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
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

        updateResizingCallbacks();
    }

    public void addResizingCallback(Consumer<Window> resizingCallback) {
        resizingCallbacks.add(resizingCallback);
    }

    public void updateResizingCallbacks() {
        for (Consumer<Window> matrixUser : resizingCallbacks) {
            matrixUser.accept(this);
        }
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    private void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public void show() {
        glfwShowWindow(window);
        updateResizingCallbacks();
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

    public void toggleFullscreen() {
        contextTasks.add(window -> {
            long windowHandle = window.window();

            long monitor = GLFW.glfwGetPrimaryMonitor();
            GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);

            boolean isFullscreen = GLFW.glfwGetWindowMonitor(windowHandle) != MemoryUtil.NULL;

            if (isFullscreen) {
                GLFW.glfwSetWindowMonitor(
                        windowHandle,
                        MemoryUtil.NULL,
                        100,
                        100,
                        oldWindowWidth,
                        oldWindowHeight,
                        GLFW.GLFW_DONT_CARE
                );
            } else {
                int[] width = new int[1];
                int[] height = new int[1];
                GLFW.glfwGetWindowSize(windowHandle, width, height);

                oldWindowWidth = width[0];
                oldWindowHeight = height[0];

                if (vidMode != null) {
                    GLFW.glfwSetWindowMonitor(
                            windowHandle,
                            monitor,
                            0,
                            0,
                            vidMode.width(),
                            vidMode.height(),
                            GLFW.GLFW_DONT_CARE
                    );
                }
            }
        });
    }
}