package omnivoxel.client.game.player;

import omnivoxel.client.game.camera.Camera;
import omnivoxel.server.client.entity.mob.player.PlayerEntity;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.graphics.opengl.input.KeyInput;
import omnivoxel.client.game.graphics.opengl.input.MouseButtonInput;
import omnivoxel.client.game.graphics.opengl.input.MouseInput;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.request.PlayerUpdateRequest;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class PlayerController extends PlayerEntity {
    private final Client client;
    private final Camera camera;
    private final Settings settings;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final GameState gameState;
    private float speed = 6000f;
    private KeyInput keyInput;
    private MouseButtonInput mouseButtonInput;
    private MouseInput mouseInput;

    private boolean togglingWireframe;
    private boolean togglingFullscreen;
    private boolean togglingDebug;

    private int oldWindowWidth;
    private int oldWindowHeight;
    private int oldWindowX;
    private int oldWindowY;

    public PlayerController(Client client, Camera camera, Settings settings, BlockingQueue<Consumer<Long>> contextTasks, GameState gameState) {
        super("Test Player", new byte[0]);
        this.client = client;
        // TODO: Add settings for this in a menu
        this.camera = camera;
        this.settings = settings;
        this.contextTasks = contextTasks;
        this.gameState = gameState;
    }

    @Override
    public void tick(float deltaTime) {
        super.tick(deltaTime);
        boolean changeRot = false;
        if (mouseButtonInput.isMouseLocked()) {
            float moveSpeed = speed * deltaTime * ConstantGameSettings.TARGET_FPS;

            // Rotation
            double deltaX = mouseInput.getDeltaX();
            double deltaY = mouseInput.getDeltaY();

            float pitchChange = (float) deltaY / settings.getFloatSetting("sensitivity", 50f) * deltaTime * ConstantGameSettings.TARGET_FPS;
            float yawChange = (float) deltaX / settings.getFloatSetting("sensitivity", 50f) * deltaTime * ConstantGameSettings.TARGET_FPS;

            if (pitchChange != 0 || yawChange != 0) {
                changeRot = true;
                camera.rotateX(pitchChange);
                camera.rotateY(yawChange);
                setPitch(camera.getPitch());
                setYaw(camera.getYaw());
            }

            // Movement
            float moveRelativeX = (keyInput.isKeyPressed(GLFW.GLFW_KEY_D) ? moveSpeed : 0) - (keyInput.isKeyPressed(GLFW.GLFW_KEY_A) ? moveSpeed : 0);
            float moveRelativeY = (keyInput.isKeyPressed(GLFW.GLFW_KEY_SPACE) ? moveSpeed : 0) - (keyInput.isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) ? moveSpeed : 0);
            float moveRelativeZ = (keyInput.isKeyPressed(GLFW.GLFW_KEY_W) ? moveSpeed : 0) - (keyInput.isKeyPressed(GLFW.GLFW_KEY_S) ? moveSpeed : 0);
            if (moveRelativeX != 0 || moveRelativeY != 0 || moveRelativeZ != 0) {
                float length = (float) Math.sqrt(moveRelativeX * moveRelativeX + moveRelativeY * moveRelativeY + moveRelativeZ * moveRelativeZ);
                if (length != 0) {
                    moveRelativeX /= length;
                    moveRelativeY /= length;
                    moveRelativeZ /= length;
                }

                float sinYaw = org.joml.Math.sin(-yaw);
                float cosYaw = org.joml.Math.cos(-yaw);

                float moveX = -moveRelativeZ * sinYaw + moveRelativeX * cosYaw;
                float moveZ = -moveRelativeZ * cosYaw - moveRelativeX * sinYaw;

                velocityX += moveX;
                velocityY += moveRelativeY;
                velocityZ += moveZ;
            }

            // Handle actions
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_F1) && !togglingWireframe) {
                togglingWireframe = true;
                gameState.setItem("shouldRenderWireframe", !gameState.getItem("shouldRenderWireframe", Boolean.class));
            } else {
                togglingWireframe = false;
            }
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_KP_ADD)) {
                camera.setFOV(camera.getFOV() - 1);
            }
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_KP_SUBTRACT)) {
                camera.setFOV(camera.getFOV() + 1);
            }
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_PAGE_UP)) {
                speed += 0.01f;
            }
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_PAGE_DOWN)) {
                speed -= 0.01f;
            }
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_F3)) {
                togglingDebug = true;
                gameState.setItem("seeDebug", !gameState.getItem("seeDebug", Boolean.class));
            } else {
                togglingDebug = false;
            }
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_F11)) {
                if (!togglingFullscreen) {
                    togglingFullscreen = true;
                    contextTasks.add(window -> {
                        long currentWindow = GLFW.glfwGetCurrentContext();
                        long monitor = GLFW.glfwGetPrimaryMonitor();
                        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitor);

                        // Check the current window monitor state and toggle fullscreen
                        boolean isFullscreen = GLFW.glfwGetWindowMonitor(currentWindow) != MemoryUtil.NULL;

                        if (isFullscreen) {
                            // Switch to windowed mode (Restore original window size and position)
                            GLFW.glfwSetWindowMonitor(currentWindow, MemoryUtil.NULL, oldWindowX, oldWindowY, oldWindowWidth, oldWindowHeight, GLFW.GLFW_DONT_CARE);
                        } else {
                            // Save the current window size before switching to fullscreen
                            int[] width = new int[1];
                            int[] height = new int[1];
                            GLFW.glfwGetWindowSize(currentWindow, width, height);
                            oldWindowWidth = width[0];
                            oldWindowHeight = height[0];

                            int[] x = new int[1];
                            int[] y = new int[1];
                            GLFW.glfwGetWindowPos(window, x, y);
                            oldWindowX = x[0];
                            oldWindowY = y[0];

                            // Switch to fullscreen mode (use the monitor's resolution)
                            assert vidMode != null;
                            GLFW.glfwSetWindowMonitor(currentWindow, monitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
                        }
                    });
                }
            } else {
                togglingFullscreen = false;
            }
            // Handle cursor escaping
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                contextTasks.add(mouseButtonInput::unlockMouse);
            }
        } else if (mouseButtonInput.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            contextTasks.add(mouseButtonInput::lockMouse);
        }
        mouseInput.clearDelta();
        if (velocityX != 0 || velocityY != 0 || velocityZ != 0 || changeRot) {
            gameState.setItem("shouldUpdateView", true);
            gameState.setItem("shouldUpdateVisibleMeshes", true);

            client.sendRequest(new PlayerUpdateRequest(getX(), getY(), getZ(), pitch, yaw));

            camera.setPosition(x, y, z);
        }
    }

    public Camera getCamera() {
        return camera;
    }

    public void setKeyInput(KeyInput keyInput) {
        this.keyInput = keyInput;
    }

    public void setMouseButtonInput(MouseButtonInput mouseButtonInput) {
        this.mouseButtonInput = mouseButtonInput;
    }

    public void setMouseInput(MouseInput mouseInput) {
        this.mouseInput = mouseInput;
    }
}