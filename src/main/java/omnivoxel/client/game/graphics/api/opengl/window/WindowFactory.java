package omnivoxel.client.game.graphics.api.opengl.window;

import omnivoxel.client.game.graphics.api.opengl.image.Image;
import omnivoxel.client.game.graphics.api.opengl.image.ImageLoader;
import omnivoxel.common.settings.ConstantClientSettings;
import omnivoxel.common.settings.Settings;
import omnivoxel.util.log.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.util.Queue;
import java.util.function.Consumer;

public final class WindowFactory {
    public static Window createWindow(int width, int height, String title, Queue<Consumer<Window>> contextTasks, boolean vsync) throws RuntimeException {
        GLFW.glfwSetErrorCallback((error, description) -> {
            String msg = GLFWErrorCallback.getDescription(description);
            Logger.error(String.format("GLFW Error %d: %s", error, msg));
        });

        String os = System.getProperty("os.name").toLowerCase();
        if (os.equals("linux")) {
            String forced = System.getenv("GLFW_PLATFORM");
            String session = (forced != null && !forced.isBlank())
                    ? forced
                    : System.getenv("XDG_SESSION_TYPE");

            if ("wayland".equalsIgnoreCase(session)) {
                GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_WAYLAND);
            } else if ("x11".equalsIgnoreCase(session)) {
                GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
            } else {
                throw new IllegalStateException("Unsupported platform: " + session);
            }
        } else {
            Logger.warn("Unsupported platform: " + os + ". This may not work.");
        }

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        int platform = GLFW.glfwGetPlatform();
        Logger.debug("Using GLFW platform: " + (
                platform == GLFW.GLFW_PLATFORM_WAYLAND ? "Wayland" :
                        platform == GLFW.GLFW_PLATFORM_X11 ? "X11" : "Unknown"
        ) + " - \"" + GLFW.glfwGetVersionString() + "\"");

        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        assert vidmode != null;

        int x = (vidmode.width() - width) / 2;
        int y = (vidmode.height() - height) / 2;

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_X11) {
            GLFW.glfwWindowHint(GLFW.GLFW_POSITION_X, x);
            GLFW.glfwWindowHint(GLFW.GLFW_POSITION_Y, y);
        }
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        } else {
            GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
        }
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, 24);
        GLFW.glfwWindowHint(GLFW.GLFW_DOUBLEBUFFER, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 6);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        long window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);

        GLFW.glfwSwapInterval(vsync ? 1 : 0);

        GLCapabilities caps = GL.createCapabilities();

        if (ConstantClientSettings.OPENGL_DEBUG) {
            if (caps.GL_KHR_debug) {
                GL11C.glEnable(GL43C.GL_DEBUG_OUTPUT);
                GL11C.glEnable(GL43C.GL_DEBUG_OUTPUT_SYNCHRONOUS);

                GL43C.glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
                    String msg = GLDebugMessageCallback.getMessage(length, message);
                    System.err.println("OpenGL debug: source=" + source +
                            " type=" + type +
                            " id=" + id +
                            " severity=" + severity +
                            " msg=" + msg);
                }, 0L);

                Logger.info("OpenGL debug logging enabled");
            } else {
                Logger.info("OpenGL debug logging enabled, but unavailable");
            }
        } else {
            Logger.info("OpenGL debug logging disabled");
        }

        if (GLFW.glfwRawMouseMotionSupported()) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
        }

        Logger.debug(String.format("GLFW window created - title \"%s\" - position (%d, %d) - scale (%d, %d) - key %d", title, x, y, width, height, window));

        String osName = System.getProperty("os.name").toLowerCase();
        if (!osName.contains("mac") && GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
            Image img = ImageLoader.load("assets/icons/favicon.png");

            GLFWImage image = GLFWImage.malloc();
            GLFWImage.Buffer imagebf = GLFWImage.malloc(1);
            image.set(img.width(), img.height(), img.image());
            imagebf.put(0, image);

            GLFW.glfwSetWindowIcon(window, imagebf);

            imagebf.free();
            image.free();
        }

        String version = GL11.glGetString(GL11.GL_VERSION);

        return new Window(window, version, contextTasks);
    }
}
