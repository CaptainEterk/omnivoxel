package omnivoxel.client.game.player;

import omnivoxel.client.game.camera.Camera;
import omnivoxel.client.game.graphics.opengl.input.KeyInput;
import omnivoxel.client.game.graphics.opengl.input.MouseButtonInput;
import omnivoxel.client.game.graphics.opengl.input.MouseInput;
import omnivoxel.client.game.hitbox.Hitbox;
import omnivoxel.client.game.settings.ConstantGameSettings;
import omnivoxel.client.game.settings.Settings;
import omnivoxel.client.game.state.State;
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
    private static final float GRAVITY = 0.08f;
    private static final float JUMP_VELOCITY = 1f * ConstantGameSettings.TARGET_TPS;
    private static final float SPRINT_SPEED = 2.6f * ConstantGameSettings.TARGET_TPS; // Optional
    private static final float AIR_RESISTANCE = 0.91f * ConstantGameSettings.TARGET_TPS; // Multiplied every frame
    private static final float GROUND_FRICTION = 0.546f; // Like stone in Minecraft

    private static final byte COLLISION_X = 0b001;
    private static final byte COLLISION_Y = 2;
    private static final byte COLLISION_Z = 4;
    private static final byte COLLISION_DONE = 0b111;

    private final Client client;
    private final Camera camera;
    private final Settings settings;
    private final BlockingQueue<Consumer<Long>> contextTasks;
    private final State state;
    private final ClientWorld world;
    private final IDCache<String, String> blockHitbox;
    private final IDCache<String, BlockHitbox> blockHitboxCache;
    private final Hitbox hitbox;

    private float x;
    private float y = -40;
    private float z;

    private float velocityX;
    private float velocityY;
    private float velocityZ;

    private float yaw;
    private float pitch;
    private float speed = 0.098f / 3;
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
    private boolean onGround = false;

    public PlayerController(Client client, Camera camera, Settings settings, BlockingQueue<Consumer<Long>> contextTasks, State state, ClientWorld world) {
        this.client = client;
        this.camera = camera;
        this.settings = settings;
        this.contextTasks = contextTasks;
        this.state = state;
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

    public void tick(double deltaTime) {
        double tickDelta = deltaTime * ConstantGameSettings.TARGET_TPS;

        state.setItem("deltaTime", deltaTime);

        BooleanRef changeRot = new BooleanRef(false);
        if (mouseButtonInput.isMouseLocked()) {
            handleInput(deltaTime, deltaTime * ConstantGameSettings.TARGET_TPS, changeRot);

            if (keyInput.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                contextTasks.add(mouseButtonInput::unlockMouse);
            }
        } else if (mouseButtonInput.isMouseButtonPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            contextTasks.add(mouseButtonInput::lockMouse);
        }
        mouseInput.clearDelta();

        velocityY -= (float) (GRAVITY * tickDelta);

        float frictionFactor = (float) ((onGround ? GROUND_FRICTION : AIR_RESISTANCE) * deltaTime);
        velocityX *= frictionFactor;
        velocityZ *= frictionFactor;
        if (!onGround) {
            velocityY *= 0.98f;
        }

        state.setItem("friction_factor", frictionFactor);

        handleCollision(deltaTime / ConstantGameSettings.COLLISION_STEPS);

        state.setItem("velocity_x", velocityX);
        state.setItem("velocity_y", velocityY);
        state.setItem("velocity_z", velocityZ);
        state.setItem("on_ground", onGround);

        if (velocityX != 0 || velocityY != 0 || velocityZ != 0 || changeRot.get()) {
            state.setItem("shouldUpdateView", true);
            state.setItem("shouldUpdateVisibleMeshes", true);

            client.sendRequest(new PlayerUpdateRequest(x, y, z, pitch, yaw));

            camera.setPosition(x, y, z);
        }
    }

    private void handleCollision(double stepDeltaTime) {
        byte collisionDone = 0;

        for (int i = 0; i < ConstantGameSettings.COLLISION_STEPS; i++) {
            if ((collisionDone & COLLISION_Y) == 0) {
                double nextY = y + velocityY * stepDeltaTime;
                if (isSolidAt(x, nextY, z)) {
                    if (velocityY < 0) {
                        onGround = true;
                    }
                    velocityY = 0;
                    collisionDone |= COLLISION_Y;
                } else {
                    y += (float) (velocityY * stepDeltaTime);
                    onGround = false;
                    collisionDone &= ~COLLISION_Y;
                }
            }

            if ((collisionDone & COLLISION_X) == 0) {
                double nextX = x + velocityX * stepDeltaTime;
                if (isSolidAt(nextX, y, z)) {
                    velocityX = 0;
                    collisionDone |= COLLISION_X;
                } else {
                    x += (float) (velocityX * stepDeltaTime);
                    collisionDone &= ~COLLISION_X;
                }
            }

            if ((collisionDone & COLLISION_Z) == 0) {
                double nextZ = z + velocityZ * stepDeltaTime;
                if (isSolidAt(x, y, nextZ)) {
                    velocityZ = 0;
                    collisionDone |= COLLISION_Z;
                } else {
                    z += (float) (velocityZ * stepDeltaTime);
                    collisionDone &= ~COLLISION_Z;
                }
            }

            if (collisionDone == COLLISION_DONE) {
                break;
            }
        }
    }

    private void handleInput(double deltaTime, double tickDelta, BooleanRef changeRot) {
        double deltaX = mouseInput.getDeltaX();
        double deltaY = mouseInput.getDeltaY();

        float pitchChange = (float) (deltaY / settings.getFloatSetting("sensitivity", 2f) * deltaTime);
        float yawChange = (float) (deltaX / settings.getFloatSetting("sensitivity", 2f) * deltaTime);

        if (pitchChange != 0 || yawChange != 0) {
            changeRot.set(true);
            camera.rotateX(pitchChange);
            camera.rotateY(yawChange);
            this.pitch = camera.getPitch();
            this.yaw = camera.getYaw();
        }

        if (onGround && keyInput.isKeyPressed(GLFW.GLFW_KEY_SPACE)) {
            velocityY = (float) (JUMP_VELOCITY * deltaTime);
            onGround = false;
        }

        float moveRelativeX = (keyInput.isKeyPressed(GLFW.GLFW_KEY_D) ? 1 : 0) - (keyInput.isKeyPressed(GLFW.GLFW_KEY_A) ? 1 : 0);
        float moveRelativeZ = (keyInput.isKeyPressed(GLFW.GLFW_KEY_W) ? 1 : 0) - (keyInput.isKeyPressed(GLFW.GLFW_KEY_S) ? 1 : 0);
        if (moveRelativeX != 0 || moveRelativeZ != 0) {
            float length = (float) Math.sqrt(moveRelativeX * moveRelativeX + moveRelativeZ * moveRelativeZ);
            if (length != 0) {
                moveRelativeX /= length;
                moveRelativeZ /= length;
            }

            float moveSpeed = (float) (speed * tickDelta);
            moveRelativeX *= moveSpeed;
            moveRelativeZ *= moveSpeed;

            float sinYaw = org.joml.Math.sin(-yaw);
            float cosYaw = org.joml.Math.cos(-yaw);

            float moveX = -moveRelativeZ * sinYaw + moveRelativeX * cosYaw;
            float moveZ = -moveRelativeZ * cosYaw - moveRelativeX * sinYaw;

            velocityX += moveX;
            velocityZ += moveZ;
        }

        // Handle actions
        if (keyInput.isKeyPressed(GLFW.GLFW_KEY_F1)) {
            if (!togglingWireframe) {
                state.setItem("shouldRenderWireframe", !state.getItem("shouldRenderWireframe", Boolean.class));
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
        if (keyInput.isKeyPressed(GLFW.GLFW_KEY_F3)) {
            if (!togglingDebug) {
                state.setItem("seeDebug", !state.getItem("seeDebug", Boolean.class));
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

                    boolean isFullscreen = GLFW.glfwGetWindowMonitor(currentWindow) != MemoryUtil.NULL;

                    if (isFullscreen) {
                        GLFW.glfwSetWindowMonitor(currentWindow, MemoryUtil.NULL, oldWindowX, oldWindowY, oldWindowWidth, oldWindowHeight, GLFW.GLFW_DONT_CARE);
                    } else {
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

                        assert vidMode != null;
                        GLFW.glfwSetWindowMonitor(currentWindow, monitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
                    }
                });
            }
            togglingFullscreen = true;
        } else {
            togglingFullscreen = false;
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

    private static class BooleanRef {
        private boolean b;

        public BooleanRef(boolean b) {
            this.b = b;
        }

        public boolean get() {
            return b;
        }

        public void set(boolean b) {
            this.b = b;
        }
    }
}