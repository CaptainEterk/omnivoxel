package omnivoxel.client.game.player;

import omnivoxel.client.game.camera.Camera;
import omnivoxel.client.game.graphics.opengl.input.KeyInput;
import omnivoxel.client.game.graphics.opengl.input.MouseButtonInput;
import omnivoxel.client.game.graphics.opengl.input.MouseInput;
import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.state.GameState;
import omnivoxel.client.game.world.ClientWorld;
import omnivoxel.client.game.world.ClientWorldChunk;
import omnivoxel.client.network.Client;
import omnivoxel.client.network.request.PlayerUpdateRequest;
import omnivoxel.util.cache.IDCache;
import omnivoxel.util.math.Position3D;
import omnivoxel.world.block.Block;
import omnivoxel.world.block.hitbox.BlockHitbox;
import omnivoxel.world.block.hitbox.FullBlockHitbox;
import omnivoxel.world.chunk.Chunk;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public class PlayerController {
    protected final float friction = 0.1f;
    private final Client client;
    private final Camera camera;
    private final Settings settings;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final GameState gameState;
    private final ClientWorld world;
    private final IDCache<String, String> blockHitbox;
    private final IDCache<String, BlockHitbox> blockHitboxCache;
    private final Hitbox hitbox;
    protected float x;
    protected float y = 100;
    protected float z;
    protected float velocityX;
    protected float velocityY;
    protected float velocityZ;
    protected float yaw;
    protected float pitch;
    private float speed = 6000f;
    private KeyInput keyInput;
    private MouseButtonInput mouseButtonInput;
    private MouseInput mouseInput;
    private boolean togglingWireframe;
    private boolean togglingFullscreen;
    private boolean togglingDebug;
    // TODO: Move to Window
    private int oldWindowWidth;
    private int oldWindowHeight;
    private int oldWindowX;
    private int oldWindowY;

    private Position3D cachedChunkPos = new Position3D(0, 0, 0);
    private Chunk<Block> cachedChunk;

    public PlayerController(Client client, Camera camera, Settings settings, BlockingQueue<Consumer<Long>> contextTasks, GameState gameState, ClientWorld world) {
        this.client = client;
        // TODO: Add settings for this in a menu
        this.camera = camera;
        this.settings = settings;
        this.contextTasks = contextTasks;
        this.gameState = gameState;
        this.world = world;
        blockHitbox = new IDCache<>();
        blockHitboxCache = new IDCache<>();
        hitbox = new Hitbox(-0.4f, -0.5f, -0.4f, 0.4f, 1.4f, 0.4f, 2, 2, 3);
    }

    private boolean isSolidAt(double wx, double wy, double wz) {
        double minX = wx + hitbox.minX() + 1;
        double maxX = wx + hitbox.maxX() + 1;
        double minY = wy + hitbox.minY();
        double maxY = wy + hitbox.maxY();
        double minZ = wz + hitbox.minZ() + 1;
        double maxZ = wz + hitbox.maxZ() + 1;

        // Loop through all blocks intersected by the hitbox
        int blockMinX = (int) Math.floor(minX);
        int blockMaxX = (int) Math.floor(maxX);
        int blockMinY = (int) Math.floor(minY);
        int blockMaxY = (int) Math.floor(maxY);
        int blockMinZ = (int) Math.floor(minZ);
        int blockMaxZ = (int) Math.floor(maxZ);

        for (int bx = blockMinX; bx <= blockMaxX; bx++) {
            for (int by = blockMinY; by <= blockMaxY; by++) {
                for (int bz = blockMinZ; bz <= blockMaxZ; bz++) {
                    int chunkX = bx >> 5;
                    int chunkY = by >> 5;
                    int chunkZ = bz >> 5;

                    int localX = bx & 31;
                    int localY = by & 31;
                    int localZ = bz & 31;

                    if (cachedChunkPos == null || cachedChunk == null ||
                            cachedChunkPos.x() != chunkX ||
                            cachedChunkPos.y() != chunkY ||
                            cachedChunkPos.z() != chunkZ) {

                        cachedChunkPos = new Position3D(chunkX, chunkY, chunkZ);
                        ClientWorldChunk clientWorldChunk = world.get(cachedChunkPos, false);
                        if (clientWorldChunk == null) return true;

                        cachedChunk = clientWorldChunk.getChunkData();
                        if (cachedChunk == null) return true;
                    }

                    Block block = cachedChunk.getBlock(localX, localY, localZ);
                    if (block != null && !"omnivoxel:air".equals(block.id())) {
                        BlockHitbox blockHitboxImpl = blockHitboxCache.get(
                                blockHitbox.get(block.id(), String.class, new Class[]{String.class}, new Object[]{"core:hitbox/full_block"}),
                                FullBlockHitbox.class
                        );

                        if (blockHitboxImpl != null) {
                            if (blockHitboxImpl.isColliding(0, 0, 0, hitbox)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public void tick(float deltaTime) {
        float frictionFactor = (float) Math.pow(friction, deltaTime);
        velocityX *= frictionFactor;
        velocityY *= frictionFactor;
        velocityZ *= frictionFactor;

        velocityY -= 0.98f;

        float stepDeltaTime = deltaTime / ConstantGameSettings.COLLISION_STEPS;

        for (int i = 0; i < ConstantGameSettings.COLLISION_STEPS; i++) {
            double nextX = x + velocityX * stepDeltaTime;
            if (isSolidAt(nextX, y, z)) {
                velocityX = 0;
            } else {
                x += velocityX * stepDeltaTime;
            }
        }

        for (int i = 0; i < ConstantGameSettings.COLLISION_STEPS; i++) {
            double nextY = y + velocityY * stepDeltaTime;
            if (isSolidAt(x, nextY, z)) {
                velocityY = 0;
            } else {
                y += velocityY * stepDeltaTime;
            }
        }

        for (int i = 0; i < ConstantGameSettings.COLLISION_STEPS; i++) {
            double nextZ = z + velocityZ * stepDeltaTime;
            if (isSolidAt(x, y, nextZ)) {
                velocityZ = 0;
            } else {
                z += velocityZ * stepDeltaTime;
            }
        }

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
                this.pitch = camera.getPitch();
                this.yaw = camera.getYaw();
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
                velocityY += moveRelativeY * 10;
                velocityZ += moveZ;
            }

            // Handle actions
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_F1)) {
                if (!togglingWireframe) {
                    gameState.setItem("shouldRenderWireframe", !gameState.getItem("shouldRenderWireframe", Boolean.class));
                }
                togglingWireframe = true;
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
                if (!togglingDebug) {
                    gameState.setItem("seeDebug", !gameState.getItem("seeDebug", Boolean.class));
                }
                togglingDebug = true;
            } else {
                togglingDebug = false;
            }
            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_F11)) {
                if (!togglingFullscreen) {
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
                togglingFullscreen = true;
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

            client.sendRequest(new PlayerUpdateRequest(x, y, z, pitch, yaw));

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